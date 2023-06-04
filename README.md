# Annex
## Introduction

Annex is an open source social network inspired by major websites like Facebook and Twitter. This project was meant to empower users by providing them with a platform where they can trust that their data is handled responsibly and ethically.
#### Source code

This is the backend repository, and the frontend repository can be found [here](https://github.com/ikiwq/new-annex-frontend).

## Installation
### Getting the files and the requisites
Clone the repository:

    git clone https://github.com/[username]/[project-name].git
Go to the project directory and install the necessary dependencies using Maven:

    cd your_directory
    mvn clean install

### Configuration
Before starting the app, we have to do a little bit of configuration.
    
#### SSL certificates
If you plan to host your server and want to use a certificate to secure your connection, go ahead in the resources/application.yaml file.

    server:
      ssl:
        key-store: classpath:certs/your_certificate
        key-store-password: your_keystore_password
        keyStoreType: PKCS12
      port: 8080
Modify the fields according to your needs.

#### Connecting to the database
Inside the application.yaml there are two fields that need to be modified in order to connect our application to our database.

The first one:

    datasource:
      url: jdbc:connector_://address:port/database
      username: your_username
      password: your_passowrd
Then, we need to modify our JPA dialect.

      hibernate:
        dialect: org.hibernate.dialect.your_dialect_here
        
For example, if I want to use MySQL, I will need to type in "org.hibernate.dialect.MySQL8Dialect".

#### Using the JWT tokens.
Inside the application.yaml file, there is also a field for RSA keys used by our JWT encrypter and decrypter.

    rsa:
      private-key: "classpath:certs/private.pem"
      public-key: "classpath:certs/public.pem"
 You will need to generate an asymmetric key. Just use a tool like OpenSSL.
  
### Usage
Start the application:

    mvn spring-boot:run
      
Access the application in your web browser at http://localhost:8080 or the port you specified in the application.yaml file, and you're ready to go!

## In depth into the code
This backend application supports cursor pagination. It's crucial to use cursor pagination since we are dealing with a social network that (should) handle thousands of new posts every minute, and a simple pagination would give us a lot of problems, such as duplicates.

The majority of services support simple cursor pagination by using post ids. Once the frontend receives a list of posts, it will take the last received one's id, and use it as a cursor.
But some services, such as liked and saved posts or the notification one, can't simply handle an aricle id as their cursor. 

The liked and saved elements are sorted by their creation date, not by the post they "contain". Since saves and likes are relationships inside the database, they have their own ids, which we can use for our pagination purposes.
Inside the DTOs folder, there's a CursorPostResponse.

    public class CursorPostsResponse {
        List<PostResponse> posts;
        Long cursor;
    }
 
It contains a list of posts and a cursor.
For example, a function that returns a user's liked posts would do:

     cursor = cursorLikes.get(0).getLikeId() + 1;
     
 As I said before, we will take the ID from the relationship, not the posts, and send it to the frontend with the posts.
