package com.example.plannerwedding

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailService {
    companion object {
        private const val TAG = "EmailService"

        // Result class to handle success/failure
        sealed class SendResult {
            object Success : SendResult()
            data class Error(val message: String) : SendResult()
        }

        // Send email using JavaMail
        suspend fun sendEmailWithJavaMail(
            context: Context,
            toEmail: String,
            subject: String,
            messageBody: String,
            fromEmail: String,
            password: String
        ): SendResult = withContext(Dispatchers.IO) {
            try {
                val props = Properties()
                props["mail.smtp.auth"] = "true"
                props["mail.smtp.starttls.enable"] = "true"
                props["mail.smtp.host"] = "smtp.gmail.com"
                props["mail.smtp.port"] = "587"

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(fromEmail, password)
                    }
                })

                val message = MimeMessage(session)
                message.setFrom(InternetAddress(fromEmail))
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                message.subject = subject
                message.setText(messageBody)

                Transport.send(message)
                Log.d(TAG, "Email sent successfully to $toEmail")
                return@withContext SendResult.Success
            } catch (e: MessagingException) {
                Log.e(TAG, "Failed to send email: ${e.message}")
                return@withContext SendResult.Error("Failed to send email: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Exception sending email: ${e.message}")
                return@withContext SendResult.Error("Exception sending email: ${e.message}")
            }
        }
    }
}