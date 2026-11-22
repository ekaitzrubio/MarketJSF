package eredua;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import businessLogic.BLFacade;
import configuration.UtilDate;
import domain.Sale;
import exceptions.MustBeLaterThanTodayException;
import exceptions.ParamNullException;
import exceptions.SaleAlreadyExistException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("eskaintza")
@RequestScoped
public class EskaintzakBean {

	private String izena;
	private String deskribapena;
	private float prezioa;
	private int egoera;
	private String data;
	private String email;
	private BLFacade facadeBL;
	private List<Sale> eskaintzak = new ArrayList();
	private String bilaketa = "";

	public EskaintzakBean() {
		facadeBL = FacadeBean.getBusinessLogic();
	}

	@PostConstruct
	public void init() {
		if (this.facadeBL == null) {
			this.facadeBL = FacadeBean.getBusinessLogic();
		}
	}

	public void sortuEskaintza() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date dataObj = UtilDate.trim(sdf.parse(data));

			Sale sale = facadeBL.createSale(izena, deskribapena, egoera, prezioa, dataObj, email, null);

			if (sale != null) {
				System.out.println("Eskaintza modu egokian sortu Da");
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Eskaintza ondo sortu da!", null));
			} else {
				System.out.println("Eskaintza ez da sortu");
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Ezin izan da eskaintza sortu. Begiratu datuak (adib. emaila).", null));
			}

		} catch (ParseException e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Data ez da baliozkoa.", null));
			e.printStackTrace();
		} catch (ParamNullException | MustBeLaterThanTodayException | SaleAlreadyExistException e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
			e.printStackTrace();
		}
	}

	public List<Sale> kontsultatuEskaintza() {
		System.out.println("bilatu");
		
		return this.eskaintzak = this.facadeBL.getSales(bilaketa);
	}

	public String getIzena() {
		return izena;
	}

	public void setIzena(String izena) {
		this.izena = izena;
	}

	public String getDeskribapena() {
		return deskribapena;
	}

	public void setDeskribapena(String deskribapena) {
		this.deskribapena = deskribapena;
	}

	public float getPrezioa() {
		return prezioa;
	}

	public void setPrezioa(float prezioa) {
		this.prezioa = prezioa;
	}

	public int getEgoera() {
		return egoera;
	}

	public void setEgoera(int egoera) {
		this.egoera = egoera;
	}

	public String getData() {
		return data;
	}

	public List<Sale> getEskaintzak() {
		return eskaintzak;
	}

	public void setEskaintzak(List<Sale> eskaintzak) {
		this.eskaintzak = eskaintzak;
	}

	public String getBilaketa() {
		return bilaketa;
	}

	public void setBilaketa(String bilaketa) {
		this.bilaketa = bilaketa;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public BLFacade getFacadeBL() {
		return facadeBL;
	}

	public void setFacadeBL(BLFacade facadeBL) {
		this.facadeBL = facadeBL;
	}

}
