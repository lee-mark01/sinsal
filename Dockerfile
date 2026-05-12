FROM ubuntu:latest
LABEL authors="seung"

ENTRYPOINT ["top", "-b"]