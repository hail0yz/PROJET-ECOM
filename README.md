# PROJET-ECOM
___

# Frontend

To run the application with Angular, go to `frontend/angularApp` directory then type the following commands:
```
ng serve
```

# Backend

- ### bookService/
  1. How to run

If you are using IntelliJ, type the following commands in a terminal while being at `bookService/` directory:
```
mvn clean
mvn install
```
then you can just type on the play button; `BookServiceApplication` should be the file to run.  

Else, you can type these commands, still in `bookService/` directory:
```
mvn clean package
java -jar target/bookService-0.0.1-SNAPSHOT.jar
```

The application now running you can type `http://localhost:8080/api/v1/books` on the research bar of your navigator to display all the books.