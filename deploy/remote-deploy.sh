#!/usr/bin/env bash
# 於伺服器執行
set -euo pipefail

compose() {
  docker compose -f docker-compose.prod.yml "$@"
}

# 公開日誌遮蔽
redact() {
  sed -E \
    -e 's/[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}/[email]/g' \
    -e 's/(re_|GK)[A-Za-z0-9_-]{8,}/[key]/g' \
    -e 's/(([Pp]assword|PASSWORD|[Ss]ecret|SECRET|[Tt]oken|TOKEN)[A-Za-z_]*["'\'']?[=:] *)[^ ,;&]+/\1[hidden]/g' \
    -e 's/[0-9a-fA-F]{32,}/[hex]/g' \
    -e 's/([0-9]{1,3}\.){3}[0-9]{1,3}/[ip]/g' \
    -e 's/([0-9a-fA-F]{1,4}:){3,}[0-9a-fA-F:]+/[ip]/g'
}

diagnose() {
  echo "::group::容器狀態"
  compose ps -a || true
  local container
  container=$(compose ps -aq reader || true)
  if [ -n "$container" ]; then
    docker inspect "$container" --format \
      'status={{.State.Status}} exit={{.State.ExitCode}} oom={{.State.OOMKilled}} restarts={{.RestartCount}}' || true
  fi
  free -m || true
  echo "::endgroup::"
  echo "::group::reader 日誌（已遮蔽）"
  compose logs --no-color --tail=80 reader 2>&1 | redact || true
  echo "::endgroup::"
}

main() {
  cd "$1"
  export DOCKERHUB_USERNAME=$2 IMAGE_TAG=$3

  compose pull
  if ! compose up -d --no-build --wait --remove-orphans; then
    diagnose
    echo "部署失敗" >&2
    return 1
  fi
  # 成功才記版本
  sed -i '/^IMAGE_TAG=/d' .env
  sed -i "\$aIMAGE_TAG=$IMAGE_TAG" .env
}

# 避免吃掉腳本
main "$@" < /dev/null
