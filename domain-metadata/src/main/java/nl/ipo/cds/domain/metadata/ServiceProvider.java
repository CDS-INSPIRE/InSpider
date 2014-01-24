package nl.ipo.cds.domain.metadata;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OrderColumn;

/**
 * @author eshuism
 * 13 jan 2012
 * XML-Schema: ServiceProvider
 */
@Entity
public class ServiceProvider extends BaseDomainObject {

	public ServiceProvider() {
		super();
	}

	public ServiceProvider(String providerName) {
		super();
		this.providerName = providerName;
	}

	@Column(nullable=true, columnDefinition="text") // Is Mandatory for WFS, but Optional for WMS
	private String providerName;
	
	@Column(nullable=true, columnDefinition="text")
	@org.hibernate.validator.constraints.URL(message="Onjuiste url")
	private String providerSite;

	//XML-schema: ResponsiblePartySubsetType
	@Column(nullable=true, columnDefinition="text")
	private String individualName;
	
	@Column(nullable=true, columnDefinition="text")
	private String positionName;
	
	@Column(nullable=true, columnDefinition="text")
	private String organizationName; //XML-schema: ResponsiblePartyType

	@Column(nullable=true, columnDefinition="text") // Strange: The WFS uses "ResponsiblePartySubsetType" where role is optional but there is also a ResponsiblePartyType where role is mandatory
	private String role;
	
	//XML-Schema: ContactType
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(name="sp_phonenumber")
	@Column(name="phonenumber", columnDefinition="text")
	@OrderColumn(name="index")
	private List<String> phoneNumbers = new ArrayList<String>(); 
	
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(name="sp_faxnumber")
	@Column(name="faxnumber", columnDefinition="text")
	@OrderColumn(name="index")
	private List<String> faxNumbers = new ArrayList<String>();

	@Column(nullable=true, columnDefinition="text")
	@org.hibernate.validator.constraints.URL(message="Onjuiste url")
	private String onlineResource;
	
	@Column(nullable=true, columnDefinition="text")
	private String hoursOfService;
	
	@Column(nullable=true, columnDefinition="text")
	private String contactInstructions;

	//XML-schema: AddressType
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(name="sp_deliverypoint")
	@Column(name="deliverypoint", columnDefinition="text")
	@OrderColumn(name="index")
	private List<String> deliveryPoints;

	@Column(nullable=true, columnDefinition="text")
	private String city;

	@Column(nullable=true, columnDefinition="text")
	private String administrativeArea;
	
	@Column(nullable=true, columnDefinition="text")
	private String postalCode;
	
	@Column(nullable=true, columnDefinition="text")
	private String country;
	
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(name="sp_emailaddress")
	@Column(name="emailaddress", columnDefinition="text")
	@OrderColumn(name="index")
	private List<String> emailAddresses = new ArrayList<String>(); 
	
	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	public String getProviderSite() {
		return providerSite;
	}

	public void setProviderSite(String providerSite) {
		this.providerSite = providerSite;
	}

	public String getIndividualName() {
		return individualName;
	}

	public void setIndividualName(String individualName) {
		this.individualName = individualName;
	}

	public String getPositionName() {
		return positionName;
	}

	public void setPositionName(String positionName) {
		this.positionName = positionName;
	}

	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getHoursOfService() {
		return hoursOfService;
	}

	public void setHoursOfService(String hoursOfService) {
		this.hoursOfService = hoursOfService;
	}

	public String getContactInstructions() {
		return contactInstructions;
	}

	public void setContactInstructions(String contactInstructions) {
		this.contactInstructions = contactInstructions;
	}

	public List<String> getDeliveryPoints() {
		return deliveryPoints;
	}

	public void setDeliveryPoints(List<String> deliveryPoints) {
		this.deliveryPoints = deliveryPoints;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getAdministrativeArea() {
		return administrativeArea;
	}

	public void setAdministrativeArea(String administrativeArea) {
		this.administrativeArea = administrativeArea;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public List<String> getPhoneNumbers() {
		return phoneNumbers;
	}

	public void setPhoneNumbers(List<String> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	public List<String> getFaxNumbers() {
		return faxNumbers;
	}

	public void setFaxNumbers(List<String> faxNumbers) {
		this.faxNumbers = faxNumbers;
	}

	public List<String> getEmailAddresses() {
		return emailAddresses;
	}

	public void setEmailAddresses(List<String> emailAddresses) {
		this.emailAddresses = emailAddresses;
	}

	public String getEmail() {
		return this.getEmailAddresses().get(0);
	}

	public void setEmail(String email) {
		this.getEmailAddresses().add(email);
	}

	public String getPhone() {
		return this.getPhoneNumbers().get(0);
	}

	public void setPhone(String phone) {
		this.getPhoneNumbers().add(phone);
	}

	public String getFax() {
		return this.getFaxNumbers().get(0);
	}

	public void setFax(String fax) {
		this.getFaxNumbers().add(fax);
	}

	public String getOnlineResource() {
		return onlineResource;
	}

	public void setOnlineResource(String onlineResource) {
		this.onlineResource = onlineResource;
	}

}
