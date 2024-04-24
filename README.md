**Backend Repository: SecureShare-Backend**

**Description:**
The SecureShare-Backend repository contains the backend implementation of the Share Securely Services (S3) project, a web service designed to allow multiple authenticated users to securely share various types of items, including pictures, videos, text, and binary files. This project is developed using MongoDB, Spring Framework, Spring Security, and Java concepts to ensure robust authentication, authorization, and data management.

**Key Features:**
- **User Authentication and Authorization:** Utilizes Spring Security for secure user authentication and authorization. Supports two types of users: general users and administrators.
- **Item Management:** Allows users to perform CRUD operations on items, including viewing, putting, getting, and removing items. Items can be pictures, videos, text, or binary files.
- **Group Management:** Supports the creation and management of multiple groups. Users can join existing groups or create new ones, with group membership requiring acceptance by administrators.
- **Operations on Items:** Provides operations such as getting, removing, and viewing items. Users can retrieve items, delete their own items, and view all items in groups they are members of.
- **Secure Storage:** Stores items in a secure and retrievable form, likely utilizing a database such as MongoDB for data storage.

**Implementation Details:**
- **Programming Language:** Java 15
- **Database:** MongoDB (or any suitable relational database)
- **Framework:** Spring Framework (including Spring Boot for rapid application development)
- **Security:** Implements secure practices to prevent common vulnerabilities such as script attacks, overflows, injections, timing attacks, and authentication issues.
- **Middleware:** Utilizes Spring Security for user authentication and authorization.
- **RESTful API:** Provides a RESTful API to handle various operations on items and groups.

**Installation and Setup:**
1. Clone the repository to your local machine.
2. Ensure that Java Development Kit (JDK) and Apache Maven are installed.
3. Set up a MongoDB instance and configure the application.properties file with the database connection details.
4. Run the application using Maven or your preferred IDE.

**Contributing:**
Contributions to the SecureShare-Backend repository are welcome! Please follow the guidelines outlined in the CONTRIBUTING.md file for submitting pull requests, reporting bugs, or suggesting new features.

**Changes to make in Code for running it:**
https://www.mongodb.com/products/platform/cloud

go to the above url and signup and host your MongoDB and update it in spring application, in application.properties
```
spring.data.mongodb.uri=mongodb+srv://root:root@cluster0.crkygpz.mongodb.net/test
```
 or you can set-up mongodb locally and set-up the properties.

 For front-end code:
 
 refer ( Developed using Angular 8): 
 ```
 https://github.com/Manideep2233/secureshare_frontend
 ```
