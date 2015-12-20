# Worldguard RegionInfo

## Features

- 스코어보드를 이용해 영역이름과 소유자를 보여주는 HUD 기능
- 보호영역에 들어올 때와 나갈 때 커스텀 Title & Subtitle 띄우기
- 간단한 명령어를 통한 Title 편집과 그룹별 설정이 가능
- Developed and tested on SpigotMC 1.8.3

## Downloads
https://github.com/ASeomHan/WG_RegionInfo/releases

## Commands

영역 정보 HUD 토글 (자신것만)
> /rghud

"spawn" 영역에 유저가 들어가면 "Hello!" Title & Subtitle 띄우기
> /rginfo enter-title spawn Hello!

> /rginfo enter-subtitle spawn Hello!

"spawn" 영역에서 유저가 나가면 "Bye!" Title & Subtitle 띄우기
> /rginfo leave-title spawn Bye!

> /rginfo leave-subtitle spawn Bye!

이미 지정된 것을 삭제하려면 내용을 입력하지 않으면 됩니다
> /rginfo enter-title spawn

> ...

설정 리로드
> /rginfo reload

## [Advanced] Group configuration

"home" 그룹을 만들고 "my-home"과 "your-home" 두개의 Region을 추가하기
> /rginfo newgroup home

> /rginfo addregion home my-home your-home

"home" 그룹에 포함되는 모든 Region에 enter-title 지정하기
> /rginfo enter-title g:home Hello!

이미 지정된 것을 삭제하기
> /rginfo enter-title g:home

"home" 그룹 삭제하기
> /rginfo delgroup home
