package com.model2.mvc.web.product;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.model2.mvc.common.Page;
import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.product.ProductService;
import com.model2.mvc.service.product.impl.ProductServiceImpl;

@Controller
@RequestMapping("/product/*")
public class ProductController {
	@Autowired
	@Qualifier("productServiceImpl")
	private ProductService productService;
	
	public ProductController() {
		System.out.println(this.getClass());
	}

	
	@Value("#{commonProperties['pageUnit']}")
	//@Value("#{commonProperties['pageUnit'] ?: 3}")
	int pageUnit;
	
	@Value("#{commonProperties['pageSize']}")
	//@Value("#{commonProperties['pageSize'] ?: 2}")
	int pageSize;
	
	
	//@RequestMapping("/addProductView.do")
	@RequestMapping(value="addProduct",method=RequestMethod.GET)
	public ModelAndView addProductView() throws Exception {

		System.out.println("/addProduct : GET");
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/product/addProductView.jsp");
		return modelAndView;
	}
	
	//@RequestMapping("/addProduct.do")
	@RequestMapping(value="addProduct",method=RequestMethod.POST)
	public ModelAndView addProduct( @ModelAttribute("product") Product product , HttpServletRequest request) throws Exception {

		System.out.println("/addProduct : POST");
		//Business Logic

		
		ModelAndView modelAndView = new ModelAndView();
		
		System.out.println("======================================================================");
		if(FileUpload.isMultipartContent(request)) {
			
			String temDir = "C:\\Users\\AIA\\git\\repository2\\07MNP\\07.Model2MVCShop(URI,pattern)\\src\\main\\webapp\\images\\uploadFiles"; 
					
					
			DiskFileUpload fileUpload = new DiskFileUpload();
			fileUpload.setRepositoryPath(temDir);
			fileUpload.setSizeMax(1024*1024*10);
			fileUpload.setSizeThreshold(1024*100);
			
			if(request.getContentLength()<fileUpload.getSizeMax()) {
			
				
				StringTokenizer token = null;
				
				List fileItemList = fileUpload.parseRequest(request);
				int Size = fileItemList.size();
				for(int i=0;i<Size;i++) {
					FileItem fileItem = (FileItem) fileItemList.get(i);
					
					if(fileItem.isFormField()) {
						if(fileItem.getFieldName().equals("manuDate")) {
							token = new StringTokenizer(fileItem.getString("euc-kr"),"-");
							String manuDate = token.nextToken()+token.nextToken()+token.nextToken();
							product.setManuDate(manuDate);
						}
						else if(fileItem.getFieldName().equals("prodName"))
							product.setProdName(fileItem.getString("euc-kr"));
						else if(fileItem.getFieldName().equals("prodDetail"))
							product.setProdDetail(fileItem.getString("euc-kr"));
						else if(fileItem.getFieldName().equals("price"))
							product.setPrice(Integer.parseInt(fileItem.getString("euc-kr")));
						
					} else {
						if(fileItem.getSize()>0) {
							int idx = fileItem.getName().lastIndexOf("\\");
							if(idx==-1) {
								idx=fileItem.getName().lastIndexOf("/");
							}
							String fileName=fileItem.getName().substring(idx+1);
							product.setFileName(fileName);
							try {
								File uploadedFile = new File(temDir,fileName);
								fileItem.write(uploadedFile);
							}catch (IOException e) {
								System.out.println(e);
							}
						}else {
							product.setFileName("../../images/empty.GIF");
						}
					}//else
							
				}//for
				productService.addProduct(product);
			
				modelAndView.addObject("product", product);
				modelAndView.setViewName("forward:/product/addProduct.jsp");
				
			}else{
				int overSize = (request.getContentLength()/ 1000000);
				System.out.println("<script>alert('파일의 크기는 1MB까지 입니다. 올리신 파일 용량은" +overSize +"MB입니다');");
				System.out.println("histroy.back();</script>");
			}
		}else {
			System.out.println("인코딩 타입이 multipart/form-data가 아닙니다..");
		}
	
		
		return modelAndView;
	}
	
	//@RequestMapping("/getProduct.do")
	@RequestMapping(value="getProduct",method=RequestMethod.GET)
	public ModelAndView getProduct( @RequestParam("prodNo") int prodNo , Model model , HttpServletRequest request ,HttpServletResponse response ) throws Exception {
		
		System.out.println("/getProduct : 	GET");
		//Business Logic
		Product product = productService.getProduct(prodNo);
		// Model 과 View 연결
		
		ModelAndView modelAndView = new ModelAndView();
		model.addAttribute("product", product);
		model.addAttribute("menu",request.getParameter("menu"));
		modelAndView.addObject("product", product);
		modelAndView.addObject("menu",request.getParameter("menu"));
		modelAndView.setViewName("forward:/product/getProduct.jsp");
		
		
		
		Cookie[] cook = request.getCookies();
		Cookie cookiee = null;;
		if(cook !=null && cook.length > 0) {
			for (int i = 0; i < cook.length; i++) {
				if (cook[i].getName().equals("history")) {
					cookiee = new  Cookie("history", cook[i].getValue() + "," + prodNo);
					
					}else {
					cookiee = new Cookie("history", String.valueOf(prodNo));
				}
			}
		}
		else {
			cookiee = new Cookie("history", String.valueOf(prodNo));
			
		}
		//Cookie cookie = new Cookie("history",String.valueOf(prodNo));
		response.addCookie(cookiee);
		return modelAndView;
	}
	
	//@RequestMapping("/updateProductView.do")
	@RequestMapping(value="updateProduct",method=RequestMethod.GET)
	public ModelAndView updateProductView( @RequestParam("prodNo") int prodNo , Model model) throws Exception{

		System.out.println("/updateProduct : GET");
		//Business Logic
		Product product = productService.getProduct(prodNo);
		// Model 과 View 연결
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("product", product);
		modelAndView.setViewName("forward:/product/updateProductView.jsp");
		return modelAndView;
	}
	
	//@RequestMapping("/updateProduct.do")
	@RequestMapping(value="updateProduct",method=RequestMethod.POST)
	public ModelAndView updateProduct( @ModelAttribute("product") Product product , Model model , HttpSession session) throws Exception{

		System.out.println("/updateProduct : post");
		//Business Logic
		productService.updateProduct(product);
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/product/getProduct?prodNo="+ product.getProdNo()+"&menu=manage");
		
		
		
		return modelAndView;
	}
	
	//@RequestMapping("/listProduct.do")
	@RequestMapping(value="listProduct")
	public String listProduct( @ModelAttribute("search") Search search , Model model , HttpServletRequest request ) throws Exception{
		
		System.out.println("/listProduct");
		
		if(search.getCurrentPage() ==0 ){
			search.setCurrentPage(1);
		}
		search.setPageSize(pageSize);
		
		// Business logic 수행
		Map<String , Object> map=productService.getProductList(search);
		
		Page resultPage = new Page( search.getCurrentPage(), ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
		System.out.println(resultPage);
		
		// Model 과 View 연결
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);
		model.addAttribute("menu",request.getParameter("menu"));
		model.addAttribute("proTranCode","000");
		return "forward:/product/listProduct.jsp";

	}
	
	
}
