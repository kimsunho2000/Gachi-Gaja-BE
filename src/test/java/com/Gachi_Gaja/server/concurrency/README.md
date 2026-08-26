# 정원 초과 동시성 재현 테스트

`bd645fb` 커밋("참가 요청간 동시성 문제 해결을 위한 비관적 락 추가 및 리팩토링")에서
고친 문제를 층별로 재현한다. 정원 초과의 원인이 한 겹이 아니었기 때문에 층을 나눴다.

| 층 | 테스트 | 재현 조건 | 확인하는 것 |
|---|---|---|---|
| 1층 | `GroupCapacityConcurrencyTest#sequentialJoin_shouldRejectBeyondCapacity` | 단일 스레드 | 인원 체크 부등호(`>` vs `>=`). 동시성과 무관하게 6번째가 통과하던 off-by-one |
| 2층 | `GroupCapacityConcurrencyTest#concurrentJoin_*` | 동시 요청 | `countByGroup` → `save` 사이의 check-then-act 경합 |
| 3층 | `RepeatableReadSnapshotTest` | 두 트랜잭션 교차 | 락을 잡고도 낡은 인원 수를 세는 REPEATABLE READ 스냅샷 문제 |
| 4층 | `GroupJoinPathConsistencyTest` | 두 진입점 혼합 | 락도 정원 체크도 없던 두 번째 참가 경로(`GroupService.addMemberToGroup`) |

## 실행 방법

InnoDB 고유 동작(`FOR UPDATE` 대기, RR 스냅샷 시점)에 의존하므로 **반드시 MySQL 위에서** 돌려야 한다.
H2 인메모리로는 재현되지 않는다. 접속 정보가 없거나 MySQL이 없으면 테스트는 실패하지 않고 자동으로 skip 된다.

```bash
# 개발 DB(gachi_gaja)와 분리된 gachi_gaja_test 스키마를 자동 생성해서 쓴다
TEST_DB_USER=root TEST_DB_PASSWORD='실제비밀번호' \
  ./gradlew test --tests '*concurrency*' --console=plain -i
```

필요하면 URL도 바꿀 수 있다:

```bash
TEST_DB_URL='jdbc:mysql://localhost:3306/gachi_gaja_test?createDatabaseIfNotExist=true'
```

## 3층 테스트가 보이려는 것

`InviteService.joinGroup`의 현재 문장 순서는 다음과 같다.

```java
userRepository.findById(userId)              // (1) 일반 읽기 → 여기서 read view 고정
groupRepository.findByIdWithPessimisticLock(groupId)  // (2) 잠금 획득 (앞선 트랜잭션 커밋 대기)
memberRepository.countByGroup(group)         // (3) 일반 읽기 → (1) 시점의 낡은 스냅샷
```

MySQL InnoDB의 기본 격리 수준 REPEATABLE READ에서 일반 SELECT의 스냅샷은
**트랜잭션 내 첫 일반 읽기 시점**에 고정된다. 반면 `SELECT ... FOR UPDATE`는 항상 최신 커밋본을 읽는다.

따라서 (2)에서 락을 기다리는 동안 다른 트랜잭션이 INSERT 후 커밋해도 (3)에는 보이지 않는다.
**락은 정상 동작했지만 세어 본 값이 낡은 것이다.**

두 테스트는 (1)과 (2)의 순서만 바꿔 결과가 달라지는 것을 보인다.
잠금 읽기는 read view를 만들지 않으므로, 락을 먼저 잡으면 그 이후 첫 일반 SELECT가
새 스냅샷을 뜨면서 최신 커밋을 보게 된다. 즉 **수정은 쿼리 순서를 바꾸는 것**이다.

## 참고 — 테스트가 통과/실패할 때의 의미

- 1층 실패 → 인원 체크 부등호가 잘못됨
- 2층 실패 → check-then-act 구간이 락으로 보호되지 않음
- 3층 `snapshotOpenedBeforeLock_seesStaleCount` **통과** → 스냅샷 문제가 재현됨(= 아직 안 고쳐짐)
- 3층 `lockAcquiredFirst_seesFreshCount` 통과 → 순서를 바꾸면 해결된다는 근거

## 4층 테스트가 보이려는 것

앞의 1~3층은 전부 `InviteService.joinGroup` **한 경로**만 보고 있었다.
그런데 그룹에 사람을 넣는 입구가 하나가 아니었다.

| 입구 | 구현 | 상태 |
|---|---|---|
| `POST /api/invites/{groupId}` | `InviteService.joinGroup` | 락 + 마감일/중복/정원 체크 |
| `POST /api/groups/{groupId}/members` | `GroupService.addMemberToGroup` | **정원 체크도 락도 없이 바로 save** |

두 번째 입구에는 `MAX_GROUP_MEMBERS` 검사 자체가 없었다. 동시성 이전의 문제다.
1~3층을 아무리 잘 고쳐도 이쪽으로 들어오면 정원이 그대로 무너진다.
실제로 두 경로를 섞어 10명을 동시에 밀어 넣으면 **10명 전원이 가입에 성공**했다.

```
[혼합경로] 성공=10 거절=0 기타예외=0 최종인원=10   ← 수정 전
[혼합경로] 성공=5  거절=5 기타예외=0 최종인원=5    ← 수정 후
```

고친 방식은 두 번째 입구에 규칙을 복사하는 것이 **아니라**,
`GroupService.addMemberToGroup` 이 `InviteService.joinGroup` 에 위임하게 만드는 것이다.
규칙을 두 벌 두면 반드시 다시 갈라진다.

참가 경로를 새로 추가한다면 `GroupJoinPathConsistencyTest#joinPaths()` 목록에도 추가할 것.
그래야 새 입구가 같은 규칙을 통과하는지 자동으로 검사된다.

### 이 층에서 함께 고친 것

비관적 락이 3초 안에 잡히지 않으면 `PessimisticLockingFailureException` 이 나는데,
`GlobalExceptionHandler` 에 매핑이 없어 **500 Internal server error** 로 나가고 있었다.
이건 서버 결함이 아니라 재시도하면 되는 충돌이므로 409 로 내려준다.

### 주의 — 두 입구는 "규칙"만 공유하고 "응답"은 다르다

한쪽으로 합칠 때 응답까지 같아지면 API 명세를 깨뜨린다.
`MemberAppendRequest` 명세에는 이렇게 적혀 있다:

> 이미 추가된 User인 경우에는 따로 내부 처리를 하지 않고 마찬가지로 200 띄움

즉 `POST /api/groups/{groupId}/members` 의 중복 가입은 **의도된 멱등 200** 이지 버그가 아니다.
반면 `POST /api/invites/{groupId}` 는 기존대로 409 를 던진다.

그래서 `InviteService` 는 중복을 **예외가 아니라 결과값(`JoinResult`)** 으로 돌려주고,
각 엔드포인트가 그 값을 자기 명세대로 해석한다.

```java
JoinResult join(...)        // 규칙의 단일 구현. 중복이면 ALREADY_MEMBER 를 반환
joinGroup(...)              // /api/invites  → ALREADY_MEMBER 면 409
addMemberToGroup(...)       // /api/groups/../members → ALREADY_MEMBER 를 무시하고 200
```

예외로 처리하고 호출측에서 `catch` 하면 안 된다.
`@Transactional` 프록시 경계를 넘는 순간 트랜잭션이 rollback-only 로 마킹돼,
잡고 정상 리턴해도 커밋 시 `UnexpectedRollbackException` 이 난다.
**정상 흐름에 속하는 분기는 예외가 아니라 값으로 표현해야 한다.**

`GroupJoinPathConsistencyTest` 의 `JoinPath.throwsOnDuplicate` 가 이 차이를 담고 있다.
검사하는 것은 "응답이 같은가"가 아니라 **"결과가 같은가(멤버가 늘지 않았는가)"** 이다.
