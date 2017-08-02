run image - docker run -p 8080:8081 wiremockserver
build image - docker build -t wiremockserver .
build jar - mvn clean compile assembly:single
api - http://localhost:8080/resource/happy