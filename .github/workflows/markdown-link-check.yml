name: markdown-link-check

on:
  push:
    branches: [ 'main*' ]
    paths:
    - '**.md'
  pull_request:
    branches: [ 'main*' ]
    paths:
    - '**.md'

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - name: check out code
      uses: actions/checkout@v4

    - name: install markdown-link-check
      run: sudo npm install -g markdown-link-check

    - name: run markdown-link-check
      run: "find . -name '*.md' -print0 | xargs -0 -n1 markdown-link-check"
