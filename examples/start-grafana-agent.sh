#!/usr/bin/env bash

case "$OSTYPE" in
  linux*)
    if ! systemctl status grafana-agent-flow > /dev/null; then
      echo "starting grafana-agent-flow"
      sudo systemctl start grafana-agent-flow
    fi
    ;;
  *)
    echo "only works for Linux currently - please start grafana-agent-flow manually"
    ;;
esac


