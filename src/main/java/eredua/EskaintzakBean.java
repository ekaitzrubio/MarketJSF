package eredua;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import businessLogic.BLFacade;
import configuration.UtilDate;
import domain.Sale;
import exceptions.MustBeLaterThanTodayException;
import exceptions.ParamNullException;
import exceptions.SaleAlreadyExistException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.*;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

@Named("eskaintza")
@ViewScoped
public class EskaintzakBean implements Serializable {

	private String izena;
	private String deskribapena;
	private float prezioa;
	private int egoera;
	private String data;
	private String email;
	private BLFacade facadeBL;
	private List<Sale> eskaintzak = new ArrayList<>();
	private String bilaketa = "";
	private Sale hautatutakoEskaintza;
	
	public EskaintzakBean() {
		facadeBL = FacadeBean.getBusinessLogic();
	}

	@PostConstruct
	public void init() {
		if (this.facadeBL == null) {
			this.facadeBL = FacadeBean.getBusinessLogic();
		}
	}

	private File kargatuArgazkia() throws IOException, ServletException {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
		Part part = request.getPart("argazkia");

		if (part == null || part.getSubmittedFileName() == null || part.getSubmittedFileName().isBlank()) {
			return null;
		}

		String submittedName = Path.of(part.getSubmittedFileName()).getFileName().toString();
		String extension = "";
		int dotIndex = submittedName.lastIndexOf('.');
		if (dotIndex >= 0) {
			extension = submittedName.substring(dotIndex);
		}

		Path tempFile = Files.createTempFile("eskaintza_", extension);
		try (InputStream input = part.getInputStream()) {
			Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
		}

		File file = tempFile.toFile();
		if (ImageIO.read(file) == null) {
			Files.deleteIfExists(tempFile);
			throw new IOException("Fitxategia ez da baliozko irudi bat.");
		}

		return file;
	}

	public void sortuEskaintza() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date dataObj = UtilDate.trim(sdf.parse(data));
			File fitxategia = kargatuArgazkia();

			Sale sale = facadeBL.createSale(izena, deskribapena, egoera, prezioa, dataObj, email, fitxategia);

			if (sale != null) {
				System.out.println("Eskaintza modu egokian sortu da");
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Eskaintza ondo sortu da!", null));
			} else {
				System.out.println("Eskaintza ez da sortu");
				FacesContext.getCurrentInstance().addMessage(null, 
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ezin izan da eskaintza sortu. Begiratu datuak (adib. emaila).", null));
			}

		} catch (Exception e) {
			e.printStackTrace();
			String mezuErrotua = (e instanceof ParseException) ? "Data ez da baliozkoa." 
					: (e instanceof IOException || e instanceof ServletException) ? "Errorea argazkia igotzean." 
					: e.getMessage();

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, mezuErrotua, null));
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

	public void setData(String data) {
		this.data = data;
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
	
	public void ikusiXehetasunak(Sale sale) {
		this.hautatutakoEskaintza = sale;
	}

	public void itxiXehetasunak() {
		this.hautatutakoEskaintza = null;
	}

	public String argazkiUrl(Sale sale) {
		if (sale == null || sale.getFile() == null) {
			return null;
		}
		
		String pathStr = sale.getFile().toString();
		if (pathStr.isBlank()) {
			return null;
		}
		String fileName = java.nio.file.Path.of(pathStr).getFileName().toString();
		return "/images/" + fileName;
	}
	
	public String getArgazkiFitxategia() {
	    if (this.hautatutakoEskaintza == null || this.hautatutakoEskaintza.getFile() == null) {
	        return null;
	    }
	    String pathStr = this.hautatutakoEskaintza.getFile().toString();
	    if (pathStr.isBlank()) {
	        return null;
	    }
	    return java.nio.file.Path.of(pathStr).getFileName().toString();
	}
	
	public Sale getHautatutakoEskaintza() {
		return hautatutakoEskaintza;
	}

	public void setHautatutakoEskaintza(Sale hautatutakoEskaintza) {
		this.hautatutakoEskaintza = hautatutakoEskaintza;
	}
}