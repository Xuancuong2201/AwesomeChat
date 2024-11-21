package com.example.awesomechat.api

import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

object AccessToken {
    private const val MESSAGINGSCOPE ="https://www.googleapis.com/auth/firebase.messaging"
    fun getAccessToken() :String? {
        try {
            val jsonString ="{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"awesomechat-ad31c\",\n" +
                    "  \"private_key_id\": \"0bc399cbf18655688088cd54011a338d8f8dccb1\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCdIFnk6+QqffB0\\nUCYQhjPzD+xJ14XiyCXjmAvU1qHwtGnQJB1fXzzXF4m1i7rJfQEBZblFK1kAOGSx\\ngnqKDa5MRgr4Jgz9u+ablj4dDYSq7wRmWbZIZfudnR+XSxsrviwK9boffJnivL6n\\nh62gylgw5HX6czCvgV1b8OsjqtzXXiyVP5sKnhRtwo3d4RAcaxjdKgbGwbXvt/MN\\n7ReAoT2rpyRsqFtsn0i6BHCciIG3cYSIVhpFpymoXMmE52UXgpTAPMH/P8U1KXj6\\nl7T36wg1kKrRWXy44rgxXJKb31R6iCwXzsML2lobUZZxEh2aZIqjUpaIKlt+FVm9\\n11FF68PZAgMBAAECggEASq3gG+Yj5TBDUzzuwobOdRVZ1ETLvRwsqe4PnYK/qCRP\\ncF5h2Xzt8TseN/un9PDlQ/FBq8bs/6ZN8XJW5HuZHmZh+0w2e3hNpCA1+F/+8LE2\\ncaDs+p8CBojxxVosmU9DJphg9GsnxrS3lu9tDhFt9hr3megauSeupC6uy55/ZRp9\\n1UHT20TwXyhjePBx4ACpBFhzwQMOt0Slk+VGfSIqIpYNMdTAzAg8UJBAHEALxtpb\\n4Ub8JYrArWkL5X7UrZBSf64DksuOWveJJ+ho9BQPjTsYurZ6Ab/LFDcjO43YJfJK\\n1XH+8W508E85ttXcg6EwNrJtA7Rjgtw6S78DSANDCwKBgQDWvr2zdc+lFyZU4X9o\\nO/u0TBVMmLQSFOxUiuMYxtCGp2yr/Yfd3g90BhKerBlqD087r1cTRnR8drEy211d\\nxxhiiCaHFcjoT+c9CehHDzRHlR3s8g9hnYTvSppzPOjTv2rd76p1npi6zzcF79VI\\n2txoLeDb3XXfGV1FvVbPZvr8zwKBgQC7T+VvqVi5wf9nnKen6gODm9KzmeNhu4uA\\nl2PF23pRWnL+AVmpqg2YNwhBdFz+n5sdJ+vi+V8pm3asjngSytpSw929r29mKO2h\\nQKLdNOnTkpLm2inWZZIKZ4Gn0O5Prve9TOSiFYOuWxjw6vdlioUAHi5d9GtWd1pc\\n+diubJfu1wKBgQDL4FMh6hu1VQEjXOMlBq21QvaFvgRXll249hMdFsQq0xEtSIqR\\nbZ2mPY237xRByT8kHxfASQeWkukwq2s8+SSBh1lsrpYLWLDlFl7b9+defofmyPkp\\nZ+8pb96qwY0aW4UOJ9fhyyydTtWiYUxQ2tieX9A3bU3W/bG74EJmrhJjqQKBgQCJ\\nRynw5x0Wrv5fUmlxX8mJiAcpB8yk0Q2RheJinNMNkouaayTqeq4R86tCRWTuW80T\\n1jFEHN1Ioh6coqGPrEnLxK7bmRq8tZxKxRNXqbMBE3hEZnmpHrGknG1ir3YowiPh\\nAdzCiaADhdJGpv3/1kBZcMUsqLiOG1UHtTi/xOio6wKBgCUbfQyG3DOV9qSrQ8w/\\noMh0yVYL+jckKkM1VJgGXH3OVas+bYsc4EeCONasVtSJvK0OVO6fvRY4ZUrCus3y\\nD2CMmlMC/bSadBdBacji0iqIqmlbBLohre4BlUybg9DeWxHunlZdVJMLNAZDu5EI\\nCupJysE/AzSnho1nLKaPkXI9\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-w8en0@awesomechat-ad31c.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"118212478609864120025\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-w8en0%40awesomechat-ad31c.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}"

            val stream = ByteArrayInputStream(jsonString.toByteArray(StandardCharsets.UTF_8))

            val googleCredential = GoogleCredentials.fromStream(stream).createScoped(arrayListOf(
                MESSAGINGSCOPE))

            googleCredential.refresh()

            return googleCredential.accessToken.tokenValue
        }
        catch (e:IOException){
            return null
        }
    }
}