package hnc.tourtobee.scrapper.handler.website;

import hnc.tourtobee.scrapper.dataobject.Menu;
import hnc.tourtobee.scrapper.dataobject.Prd;
import hnc.tourtobee.scrapper.dataobject.PrdDtl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import jh.project.httpscrapper.Website;
import jh.project.httpscrapper.util.Html;

import org.apache.http.impl.client.CloseableHttpClient;

public class HanjinHandler extends _TouristAgencyHandler {

//	@Override
//	public ArrayList<Prd> scrapPrd(CloseableHttpClient httpclient,
//			Website website, HashMap<String, String> options,
//			HashSet<String> insPrds) {
//		
//		Html html = new Html(this.getHtml(httpclient, website));
//		
//		ArrayList<String> menuUrlList = getMenuUrlList(html.removeComment().getValueByClass("mainNavitopd").toString());
//		return super.scrapPrd(httpclient, website, options, insPrds);
//	}

	@Override
	public ArrayList<Menu> scrapMenu(CloseableHttpClient httpclient, Website website) {
		Html html = new Html(this.getHtml(httpclient, website));
		ArrayList<Menu> menuList = getMenuUrlList(html.removeComment().getValueByClass("mainNavitopd").toString());
		return null;
	}
	
	private ArrayList<Menu> getMenuUrlList(String htmlStr){
		ArrayList<Menu> menuList = new ArrayList<Menu>();
		Html html = new Html(htmlStr);
		System.out.println(html);
		return menuList;
	}

	
}
