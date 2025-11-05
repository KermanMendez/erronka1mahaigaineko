package service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;

import controller.Controller;
import util.XMLUtils;

public class OfflineHistoricService {

	private final String FITXATEGIA = "offlineHistoric.xml";

	private void gehituHistorialeraXml(String erabiltzaileId, Map<String, Object> datuak) throws Exception {
		Document dokumentua = XMLUtils.parseOrCreateXmlDocument("historic.xml", "historicBackup");
		Element erroa = dokumentua.getDocumentElement();

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

		XMLUtils.saveXmlDocument(dokumentua, "historic.xml");
	}

	public void gehituSarrera(String erabiltzaileId, String email, Map<String, String> eremuak) {
		try {
			Document dokumentua = XMLUtils.parseOrCreateXmlDocument(FITXATEGIA, "historicBackup");
			Element erroa = dokumentua.getDocumentElement();

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

			XMLUtils.saveXmlDocument(dokumentua, FITXATEGIA);

		} catch (Exception salbuespena) {
			salbuespena.printStackTrace();
		}
	}

	public boolean sinkronizatuLineazKanpoDBra(Boolean konektatuta) {
		if (konektatuta == null || !konektatuta) {
			System.out.println("[INFO] Lineaz kanpo - sinkronizaziorik ez");
			return false;
		}

		boolean denaSinkronizatuta = true;

		try {
			Controller kontrolatzailea = Controller.getInstance();
			Firestore db = kontrolatzailea.getDb();
			if (db == null) {
				System.err.println("[ERROR] Firestore DB ez dago eskuragarri sinkronizaziorako");
				return false;
			}

			File fitxategia = new File(FITXATEGIA);
			if (!fitxategia.exists() || fitxategia.length() == 0) {
				System.out.println("[INFO] Ez dago lineaz kanpoko historikorik sinkronizatzeko");
				return true;
			}

			System.out.println("[INFO] Hasten da lineaz kanpoko historikoaren sinkronizazioa...");

			Document dokumentua = XMLUtils.parseXmlDocument(FITXATEGIA);
			if (dokumentua == null) {
				System.err.println("[ERROR] Ezin izan da XML dokumentua kargatu");
				return false;
			}

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
					util.FirestoreUtils firestoreUtils = new util.FirestoreUtils();
					erabiltzaileDokId = firestoreUtils.getUserIdByEmail(db, email);
				}

				if (erabiltzaileDokId != null) {
					try {
						CollectionReference historia = db.collection("users").document(erabiltzaileDokId)
								.collection("historic");
						DocumentReference dokBerria = historia.document();
						ApiFuture<WriteResult> etorkizuna = dokBerria.set(datuak);
						etorkizuna.get();

						System.out.println("[INFO] Historiko sarrera bat sinkronizatuta: " + erabiltzaileDokId);

						try {
							gehituHistorialeraXml(erabiltzaileDokId, datuak);
							sinkronizatuIndizeak.add(i);
						} catch (Exception e) {
							System.err.println("[ERROR] Errorea historic.xml-ra gehitzerakoan");
							denaSinkronizatuta = false;
						}
					} catch (InterruptedException | ExecutionException e) {
						System.err.println("[ERROR] Errorea Firestore-rekin sinkronizatzerakoan");
						denaSinkronizatuta = false;
						continue;
					}
				} else {
					System.err.println("[ERROR] Ezin izan da erabiltzailea aurkitu sinkronizatzeko: " + email);
					denaSinkronizatuta = false;
				}
			}

			if (!sinkronizatuIndizeak.isEmpty()) {
				Document dokBerria = XMLUtils.createNewDocument("historicBackup");
				Element erroBerria = dokBerria.getDocumentElement();

				for (int i = 0; i < erabiltzaileak.getLength(); i++) {
					if (sinkronizatuIndizeak.contains(i))
						continue;
					org.w3c.dom.Node erabiltzaileNodo = erabiltzaileak.item(i);
					org.w3c.dom.Node inportatua = dokBerria.importNode(erabiltzaileNodo, true);
					erroBerria.appendChild(inportatua);
				}

				XMLUtils.saveXmlDocument(dokBerria, FITXATEGIA);
			}

			Document egiaztatuDok = XMLUtils.parseXmlDocument(FITXATEGIA);
			if (egiaztatuDok != null && !egiaztatuDok.getDocumentElement().hasChildNodes()) {
				boolean ezabatuta = fitxategia.delete();
				if (ezabatuta) {
					System.out.println("[INFO] Lineaz kanpoko historiko fitxategia ezabatuta (hutsik)");
				}
			}

			if (denaSinkronizatuta) {
				System.out.println("[INFO] Lineaz kanpoko historiko guztia ondo sinkronizatuta");
			} else {
				System.out.println("[ABISUA] Lineaz kanpoko historiko batzuk ezin izan dira sinkronizatu");
			}

		} catch (Exception e) {
			System.err.println("[ERROR] Errorea sinkronizazio prozesuan");
			e.printStackTrace();
			return false;
		}

		return denaSinkronizatuta;
	}
}
