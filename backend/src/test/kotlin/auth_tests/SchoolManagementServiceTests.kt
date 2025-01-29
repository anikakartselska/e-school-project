package auth_tests

import com.nevexis.backend.SpringBackendApplication
import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.security.auth.AuthenticationRequest
import com.nevexis.backend.schoolManagement.security.reset_password.PasswordResetService
import com.nevexis.backend.schoolManagement.users.User
import com.nevexis.backend.schoolManagement.users.UserService
import com.nevexis.backend.schoolManagement.users.user_security.UserSecurityService
import org.assertj.core.api.SoftAssertions
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.math.BigDecimal


@SpringBootTest(classes = [SpringBackendApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SoftAssertionsExtension::class)
class SchoolManagementServiceTests : BaseService() {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userSecurityService: UserSecurityService

    @Autowired
    private lateinit var passwordResetService: PasswordResetService

    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    @InjectSoftAssertions private lateinit var softly: SoftAssertions


    @Test
    fun usersLoadTest() {
        val users = userService.getAllUserViewsBySchool(BigDecimal.ONE, BigDecimal.ONE)
        softly.assertThat(users.isNotEmpty()).`as`("Users not empty test")
    }

    @Test
    fun userLoginTest() {
        val response: ResponseEntity<Any> = testRestTemplate
            .postForEntity(
                "http://localhost:8080/auth/authenticate",
                AuthenticationRequest("anikakartselska", "anikaa33#")
            )
        softly.assertThat(response.statusCode == HttpStatus.OK)
        softly.assertThat(response.body != null && response.body is User)
        softly.assertThat((response.body as? User)?.username == "anikakartselska")
    }

    @Test
    fun findUserByPhoneNumberTest() {
        val response: User? = userSecurityService.findUserWithAllApprovedRolesByPhoneNumber("0134567843")
        softly.assertThat(response != null)

        val response2: User? = userSecurityService.findUserWithAllApprovedRolesByPhoneNumber("1111111111")
        softly.assertThat(response2 == null)
    }

    @Test
    fun emailSenderTest() {
       val emailSendSuccessfully = passwordResetService.resetPassword("anika1931@abv.bg")
        softly.assertThat(emailSendSuccessfully)
    }
}