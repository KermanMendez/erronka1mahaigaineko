package model;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import controller.Controller;

public class OfflineHistoric {

	private final String FITXATEGIA = "offlineHistoric.xml";

	private void gehituHistorialeraXml(String erabiltzaileId, Map<String, Object> datuak) throws Exception {
		File fitxategia = new File("historic.xml");
		DocumentBuilderFactory fabrika = DocumentBuilderFactory.newInstance();
		DocumentBuilder eraikitzailea = fabrika.newDocumentBuilder();
		Document dokumentua;
		Element erroa;

		if (fitxategia.exists() && fitxategia.length() > 0) {
			dokumentua = eraikitzailea.parse(fitxategia);
			dokumentua.getDocumentElement().normalize();
			erroa = dokumentua.getDocumentElement();
		} else {
			dokumentua = eraikitzailea.newDocument();
			erroa = dokumentua.createElement("historicBackup");
			dokumentua.appendChild(erroa);
		}

		Element erabiltzaileElem = dokumentua.createElement("user");
		if (erabiltzaileId != null)
			erabiltzaileElem.setAttribute("uid", erabiltzaileId);

		for (Map.Entry<String, Object> sarrera : datuak.entrySet()) {
			Element eremua = dokumentua.createElement(sarrera.getKey());
			String balioa = sarrera.getValue() != null ? String.valueOf(sarrera.getValue()) : "";
			eremua.appendChild(dokumentua.createTextNode(balioa));
			erabiltzaileElem.appendChild(eremua);
		}

		erroa.appendChild(erabiltzaileElem);

		TransformerFactory transfFabrika = TransformerFactory.newInstance();
		Transformer transf = transfFabrika.newTransformer();
		transf.setOutputProperty(OutputKeys.INDENT, "yes");
		transf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		DOMSource iturria = new DOMSource(dokumentua);
		try (FileOutputStream fos = new FileOutputStream(fitxategia)) {
			StreamResult emaitza = new StreamResult(fos);
			transf.transform(iturria, emaitza);
		}
	}

	public void gehituSarrera(String erabiltzaileId, String email, Map<String, String> eremuak) {
		try {
			File fitxategia = new File(FITXATEGIA);
			DocumentBuilderFactory fabrika = DocumentBuilderFactory.newInstance();
			DocumentBuilder eraikitzailea = fabrika.newDocumentBuilder();
			Document dokumentua;
			Element erroa;

			if (fitxategia.exists() && fitxategia.length() > 0) {
				dokumentua = eraikitzailea.parse(fitxategia);
				dokumentua.getDocumentElement().normalize();
				erroa = dokumentua.getDocumentElement();
			} else {
				dokumentua = eraikitzailea.newDocument();
				erroa = dokumentua.createElement("historicBackup");
				dokumentua.appendChild(erroa);
			}

			Element erabiltzaileElem = dokumentua.createElement("user");
			if (erabiltzaileId != null)
				erabiltzaileElem.setAttribute("uid", erabiltzaileId);
			if (email != null)
				erabiltzaileElem.setAttribute("email", email);

			for (Map.Entry<String, String> sarrera : eremuak.entrySet()) {
				Element eremua = dokumentua.createElement(sarrera.getKey());
				eremua.appendChild(dokumentua.createTextNode(sarrera.getValue() != null ? sarrera.getValue() : ""));
				erabiltzaileElem.appendChild(eremua);
			}

			erroa.appendChild(erabiltzaileElem);

			TransformerFactory transfFabrika = TransformerFactory.newInstance();
			Transformer transf = transfFabrika.newTransformer();
			transf.setOutputProperty(OutputKeys.INDENT, "yes");
			transf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			DOMSource iturria = new DOMSource(dokumentua);
			try (FileOutputStream fos = new FileOutputStream(fitxategia)) {
				StreamResult emaitza = new StreamResult(fos);
				transf.transform(iturria, emaitza);
			}

		} catch (Exception salbuespena) {
			salbuespena.printStackTrace();
		}
	}

	public boolean sinkronizatuLineazKanpoDBra(Boolean konektatuta) {
		if (konektatuta == null || !konektatuta) {
			return false;
		}

		boolean denaSinkronizatuta = true;

		try {
			Controller kontrolatzailea = Controller.getInstance();
			Firestore db = kontrolatzailea.getDb();
			if (db == null) {
				return false;
			}

			File fitxategia = new File(FITXATEGIA);
			if (!fitxategia.exists() || fitxategia.length() == 0) {
				return true;
			}

			DocumentBuilderFactory fabrika = DocumentBuilderFactory.newInstance();
			DocumentBuilder eraikitzailea = fabrika.newDocumentBuilder();
			Document dokumentua = eraikitzailea.parse(fitxategia);
			dokumentua.getDocumentElement().normalize();

			NodeList erabiltzaileak = dokumentua.getElementsByTagName("user");
			List<Integer> sinkronizatuIndizeak = new ArrayList<>();

			for (int i = 0; i < erabiltzaileak.getLength(); i++) {
				org.w3c.dom.Node erabiltzaileNodo = erabiltzaileak.item(i);
				if (erabiltzaileNodo.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
					continue;
				}
				Element erabiltzaileElem = (Element) erabiltzaileNodo;
				String emailAtributua = erabiltzaileElem.getAttribute("email");
				String email = emailAtributua != null && !emailAtributua.isEmpty() ? emailAtributua : null;

				Map<String, Object> datuak = new HashMap<>();
				org.w3c.dom.NodeList haurrak = erabiltzaileElem.getChildNodes();

				for (int c = 0; c < haurrak.getLength(); c++) {
					org.w3c.dom.Node haurNodo = haurrak.item(c);
					if (haurNodo.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE)
						continue;
					Element fe = (Element) haurNodo;
					String gakoa = fe.getTagName();
					String balioa = fe.getTextContent();

					if (balioa != null && balioa.matches("^-?\\d+$")) {
						try {
							datuak.put(gakoa, Integer.parseInt(balioa));
						} catch (NumberFormatException nfe) {
							datuak.put(gakoa, balioa);
						}
					} else if (balioa != null && (balioa.equalsIgnoreCase("true") || balioa.equalsIgnoreCase("false")
							|| balioa.equalsIgnoreCase("bai") || balioa.equalsIgnoreCase("ez"))) {
						if (balioa.equalsIgnoreCase("true") || balioa.equalsIgnoreCase("bai"))
							datuak.put(gakoa, true);
						else
							datuak.put(gakoa, false);
					} else {
						datuak.put(gakoa, balioa);
					}
				}

				String erabiltzaileDokId = null;
				if (email != null) {
					QuerySnapshot erabiltzaileQuery = db.collection("users").whereEqualTo("email", email).get().get();
					if (!erabiltzaileQuery.isEmpty()) {
						erabiltzaileDokId = erabiltzaileQuery.getDocuments().get(0).getId();
					}
				}

				if (erabiltzaileDokId != null) {
					try {
						CollectionReference historia = db.collection("users").document(erabiltzaileDokId)
								.collection("historic");
						DocumentReference dokBerria = historia.document();
						ApiFuture<WriteResult> etorkizuna = dokBerria.set(datuak);
						etorkizuna.get();

						try {
							gehituHistorialeraXml(erabiltzaileDokId, datuak);
							sinkronizatuIndizeak.add(i);
						} catch (Exception e) {
							System.err.println("Error appending to historic.xml: " + e.getMessage());
							denaSinkronizatuta = false;
						}
					} catch (InterruptedException | ExecutionException e) {
						System.err.println("Error al sincronizar con Firestore: " + e.getMessage());
						denaSinkronizatuta = false;
						continue;
					}
				} else {
					System.err.println("No se pudo encontrar el usuario para sincronizar");
					denaSinkronizatuta = false;
				}
			}

			if (!sinkronizatuIndizeak.isEmpty()) {
				Document dokBerria = eraikitzailea.newDocument();
				Element erroBerria = dokBerria.createElement("historicBackup");
				dokBerria.appendChild(erroBerria);

				for (int i = 0; i < erabiltzaileak.getLength(); i++) {
					if (sinkronizatuIndizeak.contains(i))
						continue;
					org.w3c.dom.Node erabiltzaileNodo = erabiltzaileak.item(i);
					org.w3c.dom.Node inportatua = dokBerria.importNode(erabiltzaileNodo, true);
					erroBerria.appendChild(inportatua);
				}

				TransformerFactory transfFabrika = TransformerFactory.newInstance();
				Transformer transf = transfFabrika.newTransformer();
				transf.setOutputProperty(OutputKeys.INDENT, "yes");
				transf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				transf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

				DOMSource iturria = new DOMSource(dokBerria);
				try (FileOutputStream fos = new FileOutputStream(fitxategia)) {
					StreamResult emaitza = new StreamResult(fos);
					transf.transform(iturria, emaitza);
				}
			}

			Document egiaztatuDok = eraikitzailea.parse(fitxategia);
			egiaztatuDok.getDocumentElement().normalize();
			if (!egiaztatuDok.getDocumentElement().hasChildNodes()) {
				fitxategia.delete();
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return denaSinkronizatuta;
	}
}
