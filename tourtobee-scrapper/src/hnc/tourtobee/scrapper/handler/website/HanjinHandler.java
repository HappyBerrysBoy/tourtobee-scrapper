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
		ArrayList<Menu> menuList = getMenuUrlList(website, html.removeComment().getValueByClass("mainNavitopd").toString());
		return null;
	}
	
	
	private ArrayList<Menu> getMenuUrlList(Website website, String htmlStr){
		ArrayList<Menu> menuList = new ArrayList<Menu>();
		Menu menu = new Menu();
		String menuName = "";
		String menuUrl = "";
		Html html = new Html(htmlStr.substring(2));
		
		Html categoryHtml = new Html(html.getTag("div").toString().substring(2));
		categoryHtml = categoryHtml.removeTag("div");
		
		while(categoryHtml.getTag("div").toString().length() > 0){
			Html subMenuHtml = categoryHtml.getTag("div");
			
			while(subMenuHtml.getTag("li").toString().length() > 0){
				menuUrl = subMenuHtml.getTag("li").getTag("a").findRegex("href=[ \"']+[\\s\\S]*['\"]+").toString().replaceAll("href=", "").replaceAll("['\"]", "");
				menuUrl = website.getUrl() + menuUrl;
				menuName = subMenuHtml.getTag("li").getTag("a").removeAllTags().toString();
				menu = new Menu();
				menu.setMenuName(menuName);
				menu.setMenuUrl(menuUrl);
				menuList.add(menu);
				
				subMenuHtml = subMenuHtml.removeTag("li");
			}
			
			categoryHtml = categoryHtml.removeTag("div");
		}
		
		for (Menu m : menuList){
			System.out.println(m.getMenuName() + " : " + m.getMenuUrl());
		}
		
		
//		System.out.println(packageHtml.removeTag("div").getTag("div"));
		return menuList;
	}

	
}
