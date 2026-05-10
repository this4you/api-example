package this4you.apiexample.config

import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.ws.config.annotation.EnableWs
import org.springframework.ws.transport.http.MessageDispatcherServlet
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition
import org.springframework.xml.xsd.SimpleXsdSchema
import org.springframework.xml.xsd.XsdSchema

// === Слайд 4 — конфігурація Spring Web Services (SOAP) ===
// Реєструє servlet на /ws-services/*, що приймає SOAP-запити,
// та публікує WSDL за адресою /ws-services/books.wsdl
@EnableWs
@Configuration
class SoapConfig {

    @Bean
    fun messageDispatcherServlet(ctx: ApplicationContext): ServletRegistrationBean<MessageDispatcherServlet> {
        val servlet = MessageDispatcherServlet()
        servlet.setApplicationContext(ctx)
        servlet.isTransformWsdlLocations = true
        return ServletRegistrationBean(servlet, "/ws-services/*")
    }

    // Bean має бути названий так само, як в URL: /ws-services/books.wsdl
    @Bean(name = ["books"])
    fun defaultWsdl11Definition(booksSchema: XsdSchema): DefaultWsdl11Definition =
        DefaultWsdl11Definition().apply {
            setPortTypeName("BooksPort")
            setLocationUri("/ws-services")
            setTargetNamespace("http://this4you/apiexample/soap/books")
            setSchema(booksSchema)
        }

    @Bean
    fun booksSchema(): XsdSchema = SimpleXsdSchema(ClassPathResource("xsd/books.xsd"))

    @Bean
    fun jaxbMarshaller(): Jaxb2Marshaller = Jaxb2Marshaller().apply {
        // Скануємо пакет з JAXB POJO (Слайд 5 — серіалізація/десеріалізація XML)
        setPackagesToScan("this4you.apiexample.soap")
    }
}
