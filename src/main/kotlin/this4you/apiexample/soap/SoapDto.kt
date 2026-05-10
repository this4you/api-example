package this4you.apiexample.soap

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlRootElement

// === Слайд 4 — SOAP DTO ===
// JAXB-анотовані класи для (де)серіалізації XML.
// Namespace має збігатися з XSD!
private const val NS = "http://this4you/apiexample/soap/books"

@XmlRootElement(name = "GetBookRequest", namespace = NS)
@XmlAccessorType(XmlAccessType.FIELD)
class GetBookRequest {
    @XmlElement(namespace = NS) var id: Long = 0
}

@XmlRootElement(name = "GetBookResponse", namespace = NS)
@XmlAccessorType(XmlAccessType.FIELD)
class GetBookResponse {
    @XmlElement(namespace = NS) var id: Long = 0
    @XmlElement(namespace = NS) var title: String = ""
    @XmlElement(namespace = NS) var author: String = ""
    @XmlElement(namespace = NS) var isbn: String = ""
    @XmlElement(namespace = NS) var priceUah: Int = 0
}
