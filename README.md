# TextDungeon.io


Designed with simplicity in mind, TextDungeon is an open source project 
which lets people store any amount of text for personal safe-keeping 
with only a name and password.
   
All plain-text is encrypted using AES-GCM-256 with the help of BCrypt. Everything is properly salted using a secure random algorithm. 
   
What makes TextDungeon.io somewhat unique is the lack of ANY JavaScript or even Cookies.

It also goes without saying that no meta-data or personal user information is ever logged.
(This includes your IP address, browser fingerprint, operating system, password hash, etc.)

I tried my best to keep everything that involves the encryption side of things clean and simple to read, however when it comes to the servlet side of things having to do with more front-end functionalities, this was my first actual project involving a web server so the code will be quite hard to understand and I don't expect anyone to contribute.

If anyone wants to know more in detail or if you come across any bugs please feel free to email me. In advance I should mention that since all user text is isolated and locked away behind a password, I allow the user to customize their html within their text if they wish for whatever reason since the isolation does not allow XSS. 
   
