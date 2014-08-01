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

	
	public ArrayList<Menu> scrapMenu(CloseableHttpClient httpclient, Website website) {
		Html html = new Html(this.getHtml(httpclient, website));
		ArrayList<Menu> menuList = getMenuUrlList(website, html.removeComment().getValueByClass("mainNavitopd").toString());
		
		
		for (Menu menu : menuList){
			
		}
		return menuList;
	}
	
	



	@Override
	public ArrayList<Prd> scrapPrdList(CloseableHttpClient httpclient, Website website, HashMap<String, String> options, HashSet<String> insPrds) {
		Html html = new Html(this.getHtml(httpclient, website));
		ArrayList<Menu> menuList = getMenuUrlList(website, html.removeComment().getValueByClass("mainNavitopd").toString());
		
		for (Menu menu : menuList){
			Website menuSite = new Website();
			menuSite.setId(website.getId());
			menuSite.setName(website.getName());
			menuSite.setUrl(menu.getMenuUrl());
			menuSite.setMethod(website.getMethod());
			menuSite.setEncoding(website.getEncoding());
			
			html = new Html(this.getHtml(httpclient, menuSite));
			System.out.println(html.getTag("XML"));
			break;
		}
		return super.scrapPrdList(httpclient, website, options, insPrds);
	}





	private ArrayList<Menu> getMenuUrlList(Website website, String htmlStr){
		ArrayList<Menu> menuList = new ArrayList<Menu>();
		Menu menu = new Menu();
		String menuName = "";
		String menuUrl = "";
		Html html = new Html(htmlStr.substring(2));
		
		//해외패키지
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
		
		//크루즈
		categoryHtml = html.removeTag("div").removeTag("div").getTag("div");
		
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
		
		//허니문
		categoryHtml = html.removeTag("div").removeTag("div").removeTag("div").getTag("div");
		
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
		
		
		//골프
		categoryHtml = html.removeTag("div").removeTag("div").removeTag("div").removeTag("div").getTag("div");
		
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
//		부산출발은 직접 해줘야함...
		menu = new Menu();
		menu.setMenuUrl("http://www.kaltour.com/ProductOverseas/OverseasList?pkgdep=PUSSM&PKGMOK=06&LMNU=5_6_0_0&MNU2COD=174&MNUCOD=");
		menu.setMenuName("부산출발");
		menuList.add(menu);

		return menuList;
	}

	
}
