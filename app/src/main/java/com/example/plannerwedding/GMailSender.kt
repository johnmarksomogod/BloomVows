package com.example.plannerwedding

import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class GMailSender {
    private val senderEmail = "your-email@gmail.com" // Replace with your Gmail
    private val senderPassword = "your-app-password" // Use an App Password from Google

    fun sendMail(recipientEmail: String, subject: String, body: String) {
        try {
            val properties = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "587")
            }

            val session = Session.getInstance(properties, object : javax.mail.Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(senderEmail, senderPassword)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(senderEmail))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                setSubject(subject)
                setText(body)
            }

            Transport.send(message)
            println("Email sent successfully to $recipientEmail")
        } catch (e: MessagingException) {
            e.printStackTrace()
            println("Failed to send email: ${e.message}")
        }
    }
}