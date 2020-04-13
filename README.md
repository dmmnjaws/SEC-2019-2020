# SEC-2019-2020
Projeto de SEC


How to execute our project (intermidiate version)

1 - Open a terminal at project's base directory
  
  -> mvn clean install

2 - Open a terminal for server in {basedir}/Server
  
  -> mvn spring-boot:run
  
  \\\ It will ask to insert the server id
 
  -> 1 \\\ 1 is server id in this version is only one

3 - Open terminal(s) for clients in {basedir}/Client
  
  -> mvn spring-boot:run
  
  \\\ It will ask to insert the client id
 
  -> X \\\ Can be 1 or 2 if you want to add more you will need to create new keys

  \\\ It will ask the server you want to connect

  -> 1 \\\ In this case is only one possible choice

  \\\ Then you will need to insert the keystore and private key passwords

  -> clientXpassword \\\ X is client id

  \\\ Then you can insert the application commands
  
  -> register/post/read/postGeneral/readGeneral
  
  \\\ Then follow the instructions to complete the commands

Extra:

- if you want to remove the application state go to the Client/data and Server/date and erase the state files;

- if you want to have more clients you will need to generate new keys in Client/data/keys and paste the key's certificate, on the server's keys directory Server/data/keys. The commands to create the keys are the following:
    
    1 - gerar private key e certificado
    
    -> openssl req -newkey rsa:2048 -nodes -passout pass:clientXpassword -keyout clientX_private_key.key -x509 -days 365 -out            clientX_certificate.crt

    2 - converter private key e certificado para pkcs12
    
    -> openssl pkcs12 -export -in clientX_certificate.crt -inkey clientX_private_key.key -name clientX -out clientX_pkcs12.p12 -passout pass:clientXpassword

    3 - generate keystore from pkcs12 file
    
    -> keytool -importkeystore -srckeystore clientX_pkcs12.p12 -srcstoretype pkcs12 -destkeystore clientX_keystore.jks -deststoretype JKS

-------------------------------------------------------------------------------------------------------------------------------
DISCUSSÃO COM O PROFESSOR:

// FEITO em vez de guardar o estado do cliente com o sequence number, pedir o sequence number ao servidor antes de se juntar.

O que acontece se o servidor crasha no meio da escrita, ver isso.

RELATÓRIO: esquecer a introdução, o indice e a API; emfase nos ataques que evitamos e como.
