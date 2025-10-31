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

	private final String FITXATEGIA = "offlineHistorikoa.xml";

	private void gehituHistorikora(String erabiltzaileId, Map<String, Object> datuak) throws Exception {
		File fitx = new File("historikoa.xml");
		DocumentBuilderFactory fabrika = DocumentBuilderFactory.newInstance();
		DocumentBuilder sortzailea = fabrika.newDocumentBuilder();
		Document doc;
		Element erroa;
		if (fitx.exists() && fitx.length() > 0) {
			doc = sortzailea.parse(fitx);
			doc.getDocumentElement().normalize();
			erroa = doc.getDocumentElement();
		} else {
			doc = sortzailea.newDocument();
			erroa = doc.createElement("historikoBackup");
			doc.appendChild(erroa);
		}

		Element erabiltzaileElem = doc.createElement("erabiltzailea");
		if (erabiltzaileId != null)
			erabiltzaileElem.setAttribute("uid", erabiltzaileId);

		for (Map.Entry<String, Object> sarrera : datuak.entrySet()) {
			Element eremua = doc.createElement(sarrera.getKey());
			String balioa = sarrera.getValue() != null ? String.valueOf(sarrera.getValue()) : "";
			eremua.appendChild(doc.createTextNode(balioa));
			erabiltzaileElem.appendChild(eremua);
		}

		erroa.appendChild(erabiltzaileElem);

		TransformerFactory fabTrans = TransformerFactory.newInstance();
		Transformer trans = fabTrans.newTransformer();
		trans.setOutputProperty(OutputKeys.INDENT, "yes");
		trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource iturburua = new DOMSource(doc);
		try (FileOutputStream fos = new FileOutputStream(fitx)) {
			StreamResult emaitza = new StreamResult(fos);
			trans.transform(iturburua, emaitza);
		}
	}

	// Gehitu sarrera offline fitxategira
	public void gehituSarrera(String erabiltzaileId, String emaila, Map<String, String> eremuak) {
		try {
			File fitx = new File(FITXATEGIA);
			DocumentBuilderFactory fabrika = DocumentBuilderFactory.newInstance();
			DocumentBuilder sortzailea = fabrika.newDocumentBuilder();
			Document doc;
			Element erroa;
			if (fitx.exists() && fitx.length() > 0) {
				doc = sortzailea.parse(fitx);
				doc.getDocumentElement().normalize();
				erroa = doc.getDocumentElement();
			} else {
				doc = sortzailea.newDocument();
				erroa = doc.createElement("historikoBackup");
				doc.appendChild(erroa);
			}

			Element erabiltzaileElem = doc.createElement("erabiltzailea");
			if (erabiltzaileId != null)
				erabiltzaileElem.setAttribute("uid", erabiltzaileId);
			if (emaila != null)
				erabiltzaileElem.setAttribute("email", emaila);

			for (Map.Entry<String, String> sarrera : eremuak.entrySet()) {
				Element eremua = doc.createElement(sarrera.getKey());
				eremua.appendChild(doc.createTextNode(sarrera.getValue() != null ? sarrera.getValue() : ""));
				erabiltzaileElem.appendChild(eremua);
			}

			erroa.appendChild(erabiltzaileElem);

			TransformerFactory fabTrans = TransformerFactory.newInstance();
			Transformer trans = fabTrans.newTransformer();
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource iturburua = new DOMSource(doc);
			try (FileOutputStream fos = new FileOutputStream(fitx)) {
				StreamResult emaitza = new StreamResult(fos);
				trans.transform(iturburua, emaitza);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean sinkronizatuLineazKanpokoak(Boolean konektatuta) {
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

			File fitx = new File(FITXATEGIA);
			if (!fitx.exists() || fitx.length() == 0) {
				return true;
			}

			DocumentBuilderFactory fabrika = DocumentBuilderFactory.newInstance();
			DocumentBuilder sortzailea = fabrika.newDocumentBuilder();
			Document doc = sortzailea.parse(fitx);
			doc.getDocumentElement().normalize();

			NodeList erabiltzaileak = doc.getElementsByTagName("erabiltzailea");
			List<Integer> sinkronizatuak = new ArrayList<>();

			for (int i = 0; i < erabiltzaileak.getLength(); i++) {
				org.w3c.dom.Node nodoa = erabiltzaileak.item(i);
				if (nodoa.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
					continue;
				}
				Element erabiltzaileElem = (Element) nodoa;
				String emailAttr = erabiltzaileElem.getAttribute("email");

				String email = emailAttr != null && !emailAttr.isEmpty() ? emailAttr : null;
				Map<String, Object> datuak = new HashMap<>();

				org.w3c.dom.NodeList haurrak = erabiltzaileElem.getChildNodes();
				for (int c = 0; c < haurrak.getLength(); c++) {
					org.w3c.dom.Node haur = haurrak.item(c);
					if (haur.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE)
						continue;
					Element eremua = (Element) haur;
					String gakoa = eremua.getTagName();
					String balioa = eremua.getTextContent();

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

				String erabiltzaileDocId = null;
				if (email != null) {
					QuerySnapshot userQuery = db.collection("users").whereEqualTo("email", email).get().get();
					if (!userQuery.isEmpty()) {
						erabiltzaileDocId = userQuery.getDocuments().get(0).getId();
					}
				}

				if (erabiltzaileDocId != null) {
					try {
						CollectionReference historikoa = db.collection("users").document(erabiltzaileDocId)
								.collection("historic");
						DocumentReference dokBerria = historikoa.document();
						ApiFuture<WriteResult> etorkizuna = dokBerria.set(datuak);
						etorkizuna.get();

						try {
							gehituHistorikora(erabiltzaileDocId, datuak);
							sinkronizatuak.add(i);
						} catch (Exception e) {
							System.err.println("Errorea historikoa.xml eguneratzean: " + e.getMessage());
							denaSinkronizatuta = false;
						}
					} catch (InterruptedException | ExecutionException e) {
						System.err.println("Errorea Firestore sinkronizazioan: " + e.getMessage());
						denaSinkronizatuta = false;
						continue;
					}
				} else {
					System.err.println("Ezin izan da erabiltzailea aurkitu sinkronizatzeko");
					denaSinkronizatuta = false;
				}
			}

			if (!sinkronizatuak.isEmpty()) {
				Document docBerria = sortzailea.newDocument();
				Element erroBerria = docBerria.createElement("historikoBackup");
				docBerria.appendChild(erroBerria);

				for (int i = 0; i < erabiltzaileak.getLength(); i++) {
					if (sinkronizatuak.contains(i))
						continue;
					org.w3c.dom.Node nodoa = erabiltzaileak.item(i);
					org.w3c.dom.Node inportatua = docBerria.importNode(nodoa, true);
					erroBerria.appendChild(inportatua);
				}

				TransformerFactory fabTrans = TransformerFactory.newInstance();
				Transformer trans = fabTrans.newTransformer();
				trans.setOutputProperty(OutputKeys.INDENT, "yes");
				trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				DOMSource iturburua = new DOMSource(docBerria);
				try (FileOutputStream fos = new FileOutputStream(fitx)) {
					StreamResult emaitza = new StreamResult(fos);
					trans.transform(iturburua, emaitza);
				}
			}

			Document egiaztatuDoc = sortzailea.parse(fitx);
			egiaztatuDoc.getDocumentElement().normalize();
			if (!egiaztatuDoc.getDocumentElement().hasChildNodes()) {
				fitx.delete();
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return denaSinkronizatuta;
	}

}
