package de.papke.cloud.portal.service;

import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import de.papke.cloud.portal.pojo.Menu;

@Service
public class MenuService {

	private static final String MENU_CONFIG_FILE_PATH = "/menu/structure.yml";

	private static final Logger LOG = LoggerFactory.getLogger(MenuService.class); 

	private Menu menu;
	
	@PostConstruct
	public void init() {

		try {
			Yaml yaml = new Yaml();
			InputStream inputStream = this.getClass().getResourceAsStream(MENU_CONFIG_FILE_PATH);
			menu = yaml.loadAs(inputStream, Menu.class);
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public Menu getMenu() {
		return menu;
	}
}
