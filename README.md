# PROJET-ECOM
___

# Backend

- ### bookService/
  - How to run
    
Type the following commands in a terminal while being at `bookService/` directory:
```
mvn clean
mvn install
mvn spring-boot:run
```

Else, you can type these commands, still in `bookService/` directory, if you want to generate the jar file:
```
mvn clean package
java -jar target/bookService-0.0.1-SNAPSHOT.jar
```

The application now running you can type `http://localhost:8080/api/v1/books` on the research bar of your navigator to display all the books.

# Frontend
- How tu run  

To run the application with Angular, go to `frontend/angularApp` directory then type the following commands:
```
ng serve
```
You will have to type this in another terminal, with the backend still running, to display all the books.
