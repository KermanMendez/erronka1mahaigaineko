package util;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 * Gestor de recursos de la aplicación con cache para mejorar el rendimiento
 */
public class ResourceLoader {
	
	private static final Map<String, ImageIcon> iconCache = new HashMap<>();
	private static final Map<String, Image> imageCache = new HashMap<>();
	
	/**
	 * Carga un icono y lo cachea para futuras referencias
	 * @param path Ruta del recurso (ej: "/img/logo.png")
	 * @return ImageIcon cacheado
	 */
	public static ImageIcon loadIcon(String path) {
		if (iconCache.containsKey(path)) {
			return iconCache.get(path);
		}
		
		try {
			URL resource = ResourceLoader.class.getResource(path);
			if (resource != null) {
				ImageIcon icon = new ImageIcon(resource);
				iconCache.put(path, icon);
				return icon;
			}
		} catch (Exception e) {
			System.err.println("[ResourceLoader] Error cargando icono: " + path);
		}
		
		return null;
	}
	
	/**
	 * Carga un icono escalado y lo cachea
	 * @param path Ruta del recurso
	 * @param width Ancho deseado
	 * @param height Alto deseado
	 * @return ImageIcon escalado y cacheado
	 */
	public static ImageIcon loadScaledIcon(String path, int width, int height) {
		String cacheKey = path + "_" + width + "x" + height;
		
		if (iconCache.containsKey(cacheKey)) {
			return iconCache.get(cacheKey);
		}
		
		ImageIcon original = loadIcon(path);
		if (original != null) {
			Image scaled = original.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
			ImageIcon scaledIcon = new ImageIcon(scaled);
			iconCache.put(cacheKey, scaledIcon);
			return scaledIcon;
		}
		
		return null;
	}
	
	/**
	 * Carga una imagen usando Toolkit y la cachea
	 * @param path Ruta del recurso
	 * @return Image cacheada
	 */
	public static Image loadImage(String path) {
		if (imageCache.containsKey(path)) {
			return imageCache.get(path);
		}
		
		try {
			URL resource = ResourceLoader.class.getResource(path);
			if (resource != null) {
				Image image = Toolkit.getDefaultToolkit().getImage(resource);
				imageCache.put(path, image);
				return image;
			}
		} catch (Exception e) {
			System.err.println("[ResourceLoader] Error cargando imagen: " + path);
		}
		
		return null;
	}
	
	/**
	 * Pre-carga recursos en segundo plano para mejorar la experiencia
	 * @param paths Rutas de recursos a pre-cargar
	 */
	public static void preloadResources(String... paths) {
		Thread preloadThread = new Thread(() -> {
			for (String path : paths) {
				loadIcon(path);
			}
			System.out.println("[ResourceLoader] ✓ Recursos pre-cargados: " + paths.length);
		}, "ResourcePreloader");
		
		preloadThread.setPriority(Thread.MIN_PRIORITY);
		preloadThread.setDaemon(true);
		preloadThread.start();
	}
	
	/**
	 * Limpia el cache de recursos (útil para liberar memoria)
	 */
	public static void clearCache() {
		iconCache.clear();
		imageCache.clear();
		System.out.println("[ResourceLoader] Cache limpiado");
	}
	
	/**
	 * Obtiene el tamaño actual del cache
	 */
	public static int getCacheSize() {
		return iconCache.size() + imageCache.size();
	}
}
