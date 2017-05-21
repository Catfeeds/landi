package org.ld.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.ld.app.Config;

import org.ld.model.FacSta;
import org.ld.model.GroceryItem;
import org.ld.model.GroceryRunning;

import org.ld.model.Plan;
import org.ld.model.PlanDetail;
import org.ld.model.PlanProgress;
import org.ld.model.RoomItem;
import org.ld.model.User;
import org.ld.service.GuestMissionService;
import org.ld.service.ItemService;
import org.ld.service.RoomService;
import org.ld.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

@Controller
@RequestMapping("/userItem")
public class UserItemController {

	@Autowired
	private UserService userService;
	@Autowired
	private RoomService roomService;
	@Autowired
	private GuestMissionService guestService;
	@Autowired
	private ItemService itemService;

	private static Logger logger = Logger.getLogger("logRec");

	@RequestMapping("/searchItemOverview") // 物品统计
	@ResponseBody
	public Map<String, Object> searchItemOverview(HttpSession session, @RequestBody String data) {
		User curUser = (User) session.getAttribute("curUser");
		Map<String, Object> ans = new HashMap<String, Object>();
		if ((curUser.getAUTH() & (0x01 << Config.getAuths().get("rFac"))) == 0) {
			ans.put("State", "Invalid");
			return ans;
		} else {
			ans.put("State", "Valid");
		}

		JSONObject dataJson = JSONObject.parseObject(data);

		String type = dataJson.getString("type");
		int pageNumber = dataJson.getIntValue("pageNum");
		String cat = dataJson.getString("cat");
		String band = dataJson.getString("band");

		int eachPage = Config.getSettingsInt().get("list_size");
		int recordTotal = itemService.getTotal(type, cat, band);
		int pageTotal = (int) Math.ceil((float) recordTotal / eachPage);

		if (recordTotal != 0) {
			if (pageNumber > pageTotal)
				pageNumber = pageTotal;

			int st = (pageNumber - 1) * eachPage;
			List<FacSta> record = itemService.getFacByTypeCatBand(type, cat, band, st, eachPage);

			ans.put("pageList", record);
		}

		ans.put("pageNow", pageNumber);
		ans.put("pageTotal", pageTotal);
		ans.put("recordTotal", recordTotal);

		return ans;
	}
	
	@RequestMapping("/searchItemList") // 根据物品种类、子类、品牌获取物品名称
	@ResponseBody
	public Map<String, Object> searchItemList(HttpSession session, @RequestBody String data) {
		User curUser = (User) session.getAttribute("curUser");
		Map<String, Object> ans = new HashMap<String, Object>();
		if ((curUser.getAUTH() & (0x01 << Config.getAuths().get("rFac"))) == 0) {
			ans.put("State", "Invalid");
			return ans;
		} else {
			ans.put("State", "Valid");
		}

		JSONObject dataJson = JSONObject.parseObject(data);

		String type = dataJson.getString("type");
		String cat = dataJson.getString("cat");
		String band = dataJson.getString("band");

		List<FacSta> record = itemService.getFacByTypeCatBandAll(type, cat, band);

		ans.put("pageList", record);

		return ans;
	}

	@RequestMapping("/searchPlanList") // 查询所有采购计划
	@ResponseBody
	public Map<String, Object> searchPlanList(HttpSession session, @RequestBody String data) {
		User curUser = (User) session.getAttribute("curUser");
		Map<String, Object> ans = new HashMap<String, Object>();
		if ((curUser.getAUTH() & (0x01 << Config.getAuths().get("rBuy"))) == 0) {
			ans.put("State", "Invalid");
			return ans;
		} else {
			ans.put("State", "Valid");
		}

		JSONObject dataJson = JSONObject.parseObject(data);

		int pageNumber = dataJson.getIntValue("pageNum");

		int eachPage = Config.getSettingsInt().get("list_size");
		int recordTotal = itemService.getTotalPlan();
		int pageTotal = (int) Math.ceil((float) recordTotal / eachPage);

		if (recordTotal != 0) {
			if (pageNumber > pageTotal)
				pageNumber = pageTotal;

			int st = (pageNumber - 1) * eachPage;
			List<Plan> record = itemService.getPlans(st, eachPage);

			ans.put("pageList", record);
		}

		ans.put("pageNow", pageNumber);
		ans.put("pageTotal", pageTotal);
		ans.put("recordTotal", recordTotal);

		return ans;
	}

	@RequestMapping("/searchPlanByPlanId") // 根据采购计划ID查询采购计划
	@ResponseBody
	public Map<String, Plan> searchPlanByPlanId(HttpSession session, @RequestBody String data) {
		JSONObject dataJson = JSONObject.parseObject(data);
		int PlanId = dataJson.getIntValue("PlanId");
		Plan plan = itemService.searchPlanByPlanid(PlanId);

		Map<String, Plan> res = new HashMap<String, Plan>();
		res.put("plan", plan);
		return res;
	}

	@RequestMapping("/getItemType") // 查询系统物品种类（家电、家具）
	@ResponseBody
	public Set<String> getItemType(HttpSession session) {
		return Config.getItem_type();
	}

	@RequestMapping("/getItemCat") // 根据物品种类type查询物品类别Cat
	@ResponseBody
	public Set<String> getItemCat(HttpSession session, @RequestBody String data) {
		JSONObject dataJson = JSONObject.parseObject(data);
		String type = dataJson.getString("type");

		return Config.getItem_cat().get(type);
	}

	@RequestMapping("/getItemCom") // 根据物品种类type查询物品品牌Com
	@ResponseBody
	public Set<String> getItemCom(HttpSession session, @RequestBody String data) {
		JSONObject dataJson = JSONObject.parseObject(data);
		String type = dataJson.getString("type");

		return Config.getItem_com().get(type);
	}

	@RequestMapping("/newPlan") // 新增采购计划
	@ResponseBody
	public Integer newPlan(HttpSession session, @RequestBody String data) {
		User curUser = (User) session.getAttribute("curUser");
		if ((curUser.getAUTH() & (0x01 << Config.getAuths().get("wBuy"))) == 0) {
			return 0;
		}

		try {
			JSONObject dataJson = JSONObject.parseObject(data);

			Plan newPlan = new Plan();
			String name = dataJson.getString("delivery") + dataJson.getString("planName");
			newPlan.setNAME(name);
			newPlan.setSTAFF(dataJson.getString("planManager"));
			newPlan.setCOMMENT(dataJson.getString("note"));

//			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();
			newPlan.setCTIME(date);

			System.out.println("=============1");
			if (itemService.addNewPlan(newPlan) == 1) {
				System.out.println("=============2");
				double sum = 0;
				newPlan = itemService.getPlanByName(name);
				JSONObject obj = dataJson.getJSONObject("itemList");
				PlanDetail pd = new PlanDetail();
				pd.setPLAN_ID(newPlan.getID());
				for (String key : obj.keySet()) {
					JSONObject obj2 = obj.getJSONObject(key);
					pd.setALL_MONEY(obj2.getDouble("totalPrice"));
					pd.setCOMMENT(obj2.getString("comment"));
					pd.setTOTAL(obj2.getInteger("count"));
					pd.setALREADY(0);
					sum += obj2.getDouble("totalPrice");

					Integer ID = obj2.getIntValue("FAC_ID");

					if (ID == 0) {
						FacSta newFs = new FacSta();

						newFs.setNAME(obj2.getString("FAC_NAME"));
						newFs.setCAT(obj2.getString("FAC_CAT"));
						newFs.setCOMMENT("");
						newFs.setCOMPANY(obj2.getString("FAC_BRAND"));
						newFs.setFAC_NUMBER(obj2.getString("FAC_NUMBER"));
						newFs.setTYPE(obj2.getString("FAC_TYPE"));
						newFs.setFREE(0);
						newFs.setBAD(0);
						newFs.setWORKING(0);
						newFs.setTOTAL(0);

						itemService.addNewFac(newFs);
						newFs = itemService.getFacByNumber(obj2.getString("FAC_NUMBER"));
						ID = newFs.getID();
					}

					pd.setFAC_ID(ID);

					itemService.addNewPlanDetail(pd);
				}

				newPlan.setMONEY(sum);
				itemService.updatePlan(newPlan);
				return 1;
			} else
				return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	@RequestMapping("/searchPlanDetail") // 查询计划采购物品（plan_detail表）
	@ResponseBody
	public Map<String, Object> searchPlanDetail(HttpSession session, @RequestBody String data) {
		User curUser = (User) session.getAttribute("curUser");
		Map<String, Object> ans = new HashMap<String, Object>();
		if ((curUser.getAUTH() & (0x01 << Config.getAuths().get("rBuy"))) == 0) {
			ans.put("State", "Invalid");
			return ans;
		} else {
			ans.put("State", "Valid");
		}

		JSONObject dataJson = JSONObject.parseObject(data);
		int pageNumber = dataJson.getIntValue("pageNum");
		int pid = dataJson.getIntValue("planID");
		int eachPage = Config.getSettingsInt().get("list_size");
		int recordTotal = itemService.getTotalPlanDetail(pid);
		int pageTotal = (int) Math.ceil((float) recordTotal / eachPage);

		if (recordTotal != 0) {
			if (pageNumber > pageTotal)
				pageNumber = pageTotal;
			
			List<PlanDetail> record = itemService.getPlanDetails(pid, 0, eachPage);
			
			ans.put("pageList", record);
		}

		ans.put("pageNow", pageNumber);
		ans.put("pageTotal", pageTotal);
		ans.put("recordTotal", recordTotal);
		return ans;
	}
	
	@RequestMapping("/searchPlanProgress") // 查询计划执行情况（plan_progress表）
	@ResponseBody
	public Map<String, Object> searchPlanProgress(HttpSession session, @RequestBody String data) {
		User curUser = (User) session.getAttribute("curUser");
		Map<String, Object> ans = new HashMap<String, Object>();
		if ((curUser.getAUTH() & (0x01 << Config.getAuths().get("rBuy"))) == 0) {
			ans.put("State", "Invalid");
			return ans;
		} else {
			ans.put("State", "Valid");
		}

		JSONObject dataJson = JSONObject.parseObject(data);
		int pageNumber = dataJson.getIntValue("pageNum");
		int pid = dataJson.getIntValue("planID");
		int eachPage = Config.getSettingsInt().get("list_size");
		int recordTotal = itemService.getTotalPlanProgress(pid);
		int pageTotal = (int) Math.ceil((float) recordTotal / eachPage);

		if (recordTotal != 0) {
			if (pageNumber > pageTotal)
				pageNumber = pageTotal;
			
			int st = (pageNumber - 1) * eachPage;
			
			List<PlanProgress> record = itemService.getPlanProgresses(pid, st, eachPage);
			
			ans.put("pageList", record);
		}

		ans.put("pageNow", pageNumber);
		ans.put("pageTotal", pageTotal);
		ans.put("recordTotal", recordTotal);
		return ans;
	}
	
	@RequestMapping("/addPlanProgress") // 添加计划执行情况
	@ResponseBody
	public Integer addPlanProgress(HttpSession session, @RequestBody String data) {
		User curUser = (User) session.getAttribute("curUser");
		if ((curUser.getAUTH() & (0x01 << Config.getAuths().get("wBuy"))) == 0) {
			return 0;
		};
		
		JSONObject dataJson = JSONObject.parseObject(data);
		PlanProgress newPg = new PlanProgress();
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date;
		try {
			date = ft.parse(dataJson.getString("time"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		
		newPg.setTIME(date);
		newPg.setPLAN_ID(dataJson.getInteger("planID"));
		newPg.setFAC_ID(dataJson.getInteger("facID"));
		newPg.setTERM(dataJson.getInteger("term"));
		newPg.setTOTAL(dataJson.getInteger("total"));
		newPg.setSTAFF(dataJson.getString("staff"));
		newPg.setALL_MONEY(dataJson.getDouble("all_money"));
		newPg.setCOMMENT(dataJson.getString("comment"));
		
		if(itemService.addNewPlanProgress(newPg) == 1) {
			PlanDetail pd = itemService.getPlanDetailByID(dataJson.getIntValue("pdID"));
			pd.setALREADY(pd.getALREADY() + newPg.getTOTAL());
			itemService.updatePlanDetain(pd);
		} else {
			return 0;
		}
		logger.info(curUser.getNAME() + " add plan progress for " + dataJson.getInteger("planID"));
		return 1;
	}
	
	@RequestMapping("/searchFacDetail") // 根据物品ID查询物品分配到房间的情况
	@ResponseBody
	public Map<String, Object> searchFacDetail(HttpSession session, @RequestBody String data) {
		User curUser = (User) session.getAttribute("curUser");
		Map<String, Object> ans = new HashMap<String, Object>();
		if ((curUser.getAUTH() & (0x01 << Config.getAuths().get("rFac"))) == 0) {
			ans.put("State", "Invalid");
			return ans;
		} else {
			ans.put("State", "Valid");
		}

		JSONObject dataJson = JSONObject.parseObject(data);

		Integer facID = dataJson.getInteger("facID");
		int pageNumber = dataJson.getIntValue("pageNum");

		int eachPage = Config.getSettingsInt().get("list_size");
		int recordTotal = roomService.totalRowByItem(facID);
		int pageTotal = (int) Math.ceil((float) recordTotal / eachPage);

		if (recordTotal != 0) {
			if (pageNumber > pageTotal)
				pageNumber = pageTotal;

			int st = (pageNumber - 1) * eachPage;
			List<RoomItem> record = roomService.getItemByItemID(facID, st, eachPage);

			ans.put("pageList", record);
		}

		ans.put("pageNow", pageNumber);
		ans.put("pageTotal", pageTotal);
		ans.put("recordTotal", recordTotal);

		return ans;
	}
	
	@RequestMapping("/searchFacSta") // 获取指定FacSta
	@ResponseBody
	public Map<String, Object> searchFacSta(HttpSession session, @RequestBody String data) {
		User curUser = (User) session.getAttribute("curUser");
		Map<String, Object> ans = new HashMap<String, Object>();
		if ((curUser.getAUTH() & (0x01 << Config.getAuths().get("rFac"))) == 0) {
			ans.put("State", "Invalid");
			return ans;
		} else {
			ans.put("State", "Valid");
		}

		JSONObject dataJson = JSONObject.parseObject(data);

		Integer facID = dataJson.getInteger("facID");
		FacSta newFs = itemService.getFac(facID);
		ans.put("fac", newFs);
		return ans;
	}
	
	@RequestMapping("/transferFac") // 移动物品到其他房间
	@ResponseBody
	public Integer transferFac(HttpSession session, @RequestBody String data) {
		User curUser = (User) session.getAttribute("curUser");
		if ((curUser.getAUTH() & (0x01 << Config.getAuths().get("wFac"))) == 0) {
			return 0;
		}

		JSONObject dataJson = JSONObject.parseObject(data);
		
		Integer recID = dataJson.getInteger("recID");
		RoomItem ri = roomService.getCertainRIRec(recID);
		
		ri.setID(null);
		Integer newRoomId = roomService.getRoomByNumber(dataJson.getString("rNum")).getID();
		ri.setROOM_ID(newRoomId);
		
		if(roomService.insertRI(ri) == 1) {
			roomService.deleteRI(recID);
		} else {
			return 0;
		}
		logger.info(curUser.getNAME() + " move item " + recID + " to " + dataJson.getString("rNum"));
		return 1;
	}
	
	@RequestMapping("/facBad") // 物品点击报废
	@ResponseBody
	public Integer facBad(HttpSession session, @RequestBody String data) {
		User curUser = (User) session.getAttribute("curUser");
		if ((curUser.getAUTH() & (0x01 << Config.getAuths().get("wFac"))) == 0) {
			return 0;
		}

		JSONObject dataJson = JSONObject.parseObject(data);
		
		Integer recID = dataJson.getInteger("recID");
		RoomItem ri = roomService.getCertainRIRec(recID);
		FacSta fs = itemService.getFac(ri.getITEM_ID());
		
		fs.setBAD(fs.getBAD() + 1);
		fs.setWORKING(fs.getWORKING() - 1);
		
		if(roomService.deleteRI(recID) == 1)
		{
			itemService.updateFac(fs);
		} else {
			return 0;
		}
		
		logger.info(curUser.getNAME() + " let " + recID + " bad ");
		return 1;
	}
	
	@RequestMapping("/toWarehouse") // 物品回仓库
	@ResponseBody
	public Integer toWarehouse(HttpSession session, @RequestBody String data) {
		User curUser = (User) session.getAttribute("curUser");
		if ((curUser.getAUTH() & (0x01 << Config.getAuths().get("wFac"))) == 0) {
			return 0;
		}

		JSONObject dataJson = JSONObject.parseObject(data);
		
		Integer recID = dataJson.getInteger("recID");
		RoomItem ri = roomService.getCertainRIRec(recID);
		if(roomService.deleteRI(recID) == 1)
		{
			FacSta fs = itemService.getFac(ri.getITEM_ID());
			fs.setWORKING(fs.getWORKING() - 1);
			fs.setFREE(fs.getFREE() + 1);
			itemService.updateFac(fs);
		} else {
			return 0;
		}
		
		logger.info(curUser.getNAME() + " move " + recID + " to warehouse" ); 
		return 1;
	}
	
	@RequestMapping("/newDistribute") // 新分配物品
	@ResponseBody
	public Integer newDistribute(HttpSession session, @RequestBody String data) {
		User curUser = (User) session.getAttribute("curUser");
		if ((curUser.getAUTH() & (0x01 << Config.getAuths().get("wFac"))) == 0) {
			return 0;
		}

		JSONObject dataJson = JSONObject.parseObject(data);
		
		int facId = dataJson.getIntValue("facID");
		String rn = dataJson.getString("rNum");
		
		FacSta fs = itemService.getFac(facId);
		if(fs.getFREE() <= 0)
		{
			return 0;
		}
		else {
			fs.setWORKING(fs.getWORKING() + 1);
			fs.setFREE(fs.getFREE() - 1);
			itemService.updateFac(fs);
			RoomItem newRi = new RoomItem();
			newRi.setCOMM(dataJson.getString("comment"));
			newRi.setTAG(dataJson.getString("tag"));
			newRi.setITEM_ID(facId);
			newRi.setROOM_ID(roomService.getRoomByNumber(rn).getID());
			newRi.setSTATE(0);
			roomService.insertRI(newRi);
		}
		
		logger.info(curUser.getNAME() + " assign " + facId + " to " + rn );
		return 1;
	}
	
	@RequestMapping("/newFacBad") // 新报废物品
	@ResponseBody
	public Integer newFacBad(HttpSession session, @RequestBody String data) {
		User curUser = (User) session.getAttribute("curUser");
		if ((curUser.getAUTH() & (0x01 << Config.getAuths().get("wFac"))) == 0) {
			return 0;
		}

		JSONObject dataJson = JSONObject.parseObject(data);
		
		int facId = dataJson.getIntValue("facID");
		int count = dataJson.getIntValue("count");
		
		FacSta fs = itemService.getFac(facId);
		if(fs.getFREE() <= count)
		{
			return 0;
		}
		else {
			fs.setFREE(fs.getFREE() - count);
			fs.setBAD(fs.getBAD() + count);
			itemService.updateFac(fs);
		}
		
		logger.info(curUser.getNAME() + " let " + count + " " + facId + " bad ");
		return 1;
	}
	
	//小卖部
	
	@RequestMapping("/searchGoodsList") // GoodsName为null时，查询所有记录(只有当前的统计，没有历史统计记录)
	@ResponseBody
	public Map<String, Object> searchGoodsList(HttpSession session, @RequestBody String data) {
		JSONObject dataJson = JSONObject.parseObject(data);

		User curUser = (User) session.getAttribute("curUser");
		Map<String, Object> ans = new HashMap<String, Object>();

		if ((curUser.getAUTH() & (0x01 << Config.getAuths().get("rGrocery"))) == 0) {
			ans.put("State", "Invalid");
			return ans;
		} else {
			ans.put("State", "Valid");
		}

		int pageNumber = dataJson.getIntValue("pageNum");
		String goods = dataJson.getString("GoodsName");
		
		int eachPage = Config.getSettingsInt().get("list_size");
		int recordTotal = itemService.totalGrocery(goods);
		int pageTotal = (int) Math.ceil((float) recordTotal / eachPage);

		if (recordTotal != 0) {
			if (pageNumber > pageTotal)
				pageNumber = pageTotal;

			int st = (pageNumber - 1) * eachPage;
			List<GroceryItem> record = itemService.getGrocery(goods, st, eachPage);

			ans.put("dataList", record);
		}

		ans.put("pageNow", pageNumber);
		ans.put("pageTotal", pageTotal);
		ans.put("recordTotal", recordTotal);

		return ans;
	}
	
	@RequestMapping("/buyGoods")
	@ResponseBody
	public Integer buyGoods(HttpSession session, @RequestBody String data) {
		JSONObject dataJson = JSONObject.parseObject(data);

		User curUser = (User) session.getAttribute("curUser");
		
		if ((curUser.getAUTH() & (0x01 << Config.getAuths().get("wGrocery"))) == 0) {
			return 0;
		}
		
		try{
			int ID = dataJson.getIntValue("ID");
			int count = dataJson.getIntValue("count");
			double per = dataJson.getDoubleValue("per");
			double total = dataJson.getDoubleValue("total"); // money
			Date date = dataJson.getDate("date"); // YYYY-MM-DD HH-MM-SS
			
			GroceryItem gi = itemService.getCertainGrocery(ID);
			GroceryRunning gr = new GroceryRunning();
			gr.setALL_MONEY(total);
			gr.setCTIME(date);
			gr.setITEM_ID(ID);
			gr.setPER_MONEY(per);
			gr.setTOTAL(count);
			gr.setTYPE(1); // 1 buy, 2 sell, 3 use
			
			if(itemService.addGroceryRec(gr) == 1)
			{
				gi.setTOTAL(gi.getTOTAL() + count);
				gi.setAVALIABLE(gi.getAVALIABLE() + count);
				return itemService.updateGrocery(gi);
			}
			else return 0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return 0;
		}
	}
	
	@RequestMapping("/sellGoods")
	@ResponseBody
	public Integer sellGoods(HttpSession session, @RequestBody String data) {
		JSONObject dataJson = JSONObject.parseObject(data);

		User curUser = (User) session.getAttribute("curUser");
		
		if ((curUser.getAUTH() & (0x01 << Config.getAuths().get("wGrocery"))) == 0) {
			return 0;
		}
		
		try{
			int ID = dataJson.getIntValue("ID");
			int count = dataJson.getIntValue("count");
			double per = dataJson.getDoubleValue("per");
			double total = dataJson.getDoubleValue("total"); // money
			Date date = dataJson.getDate("date"); // YYYY-MM-DD HH-MM-SS
			
			GroceryItem gi = itemService.getCertainGrocery(ID);
			GroceryRunning gr = new GroceryRunning();
			gr.setALL_MONEY(total);
			gr.setCTIME(date);
			gr.setITEM_ID(ID);
			gr.setPER_MONEY(per);
			gr.setTOTAL(count);
			gr.setTYPE(2); // 1 buy, 2 sell, 3 waste
			
			if(itemService.addGroceryRec(gr) == 1)
			{
				gi.setAVALIABLE(gi.getAVALIABLE() - count);
				gi.setTOTAL_SOLD(gi.getTOTAL_SOLD() + count);
				gi.setTOTAL_BENIFIT(gi.getTOTAL_BENIFIT() + total - count * gi.getBUY_MONEY());
				return itemService.updateGrocery(gi);
			}
			else return 0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return 0;
		}
	}
	
	@RequestMapping("/wasteGoods")
	@ResponseBody
	public Integer wasteGoods(HttpSession session, @RequestBody String data) {
		JSONObject dataJson = JSONObject.parseObject(data);

		User curUser = (User) session.getAttribute("curUser");
		
		if ((curUser.getAUTH() & (0x01 << Config.getAuths().get("wGrocery"))) == 0) {
			return 0;
		}
		
		try{
			int ID = dataJson.getIntValue("ID");
			int count = dataJson.getIntValue("count");
			double per = dataJson.getDoubleValue("per");
			double total = dataJson.getDoubleValue("total"); // money
			Date date = dataJson.getDate("date"); // YYYY-MM-DD HH-MM-SS
			
			GroceryItem gi = itemService.getCertainGrocery(ID);
			GroceryRunning gr = new GroceryRunning();
			gr.setALL_MONEY(total);
			gr.setCTIME(date);
			gr.setITEM_ID(ID);
			gr.setPER_MONEY(per);
			gr.setTOTAL(count);
			gr.setTYPE(3); // 1 buy, 2 sell, 3 waste
			
			if(itemService.addGroceryRec(gr) == 1)
			{
				gi.setAVALIABLE(gi.getAVALIABLE() - count);
				gi.setTOTAL_BENIFIT(gi.getTOTAL_BENIFIT() - count * gi.getBUY_MONEY());
				
				return itemService.updateGrocery(gi);
			}
			else return 0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return 0;
		}
	}
	
	@RequestMapping("/searchAnnualSale") // id为null时，查询所有记录(只有当前的统计，没有历史统计记录)
	@ResponseBody
	public Map<String, Object> searchAnnualSale(HttpSession session, @RequestBody String data) {
		JSONObject dataJson = JSONObject.parseObject(data);

		User curUser = (User) session.getAttribute("curUser");
		Map<String, Object> ans = new HashMap<String, Object>();

		if ((curUser.getAUTH() & (0x01 << Config.getAuths().get("rGrocery"))) == 0) {
			ans.put("State", "Invalid");
			return ans;
		} else {
			ans.put("State", "Valid");
		}

		int pageNumber = dataJson.getIntValue("pageNum");
		int id = dataJson.getIntValue("id");
		Date from = dataJson.getDate("from"); // YYYY-MM-DD HH-MM-SS
		Date to = dataJson.getDate("to");
		int eachPage = Config.settingsInt.get("list_size");
		int recordTotal = itemService.totalGroceryRunning(id, from, to);
		
		int pageTotal = (int) Math.ceil((float) recordTotal / eachPage);

		if (recordTotal != 0) {
			if (pageNumber > pageTotal)
				pageNumber = pageTotal;

			int st = (pageNumber - 1) * eachPage;
			List<GroceryRunning> record = itemService.getGroceryRunning(id, st, eachPage, from, to);

			ans.put("dataList", record);
		}

		ans.put("pageNow", pageNumber);
		ans.put("pageTotal", pageTotal);
		ans.put("recordTotal", recordTotal);

		return ans;
	}
	
	@RequestMapping("/addGoods") // 新增小卖部物品
	@ResponseBody
	public Integer addGoods(HttpSession session,  @RequestBody String data) {
		JSONObject dataJson = JSONObject.parseObject(data);
		
		User curUser = (User) session.getAttribute("curUser");

		if ((curUser.getAUTH() & (0x01 << Config.getAuths().get("wGrocery"))) == 0) {
			return 0;
		}
		
		try{
			String name = dataJson.getString("name");
			String type = dataJson.getString("type");
			String cat = dataJson.getString("cat");
			double buyMoney = dataJson.getDoubleValue("buyPrice");
			double sellMoney = dataJson.getDoubleValue("sellPrice");
			int total = dataJson.getIntValue("total");
			GroceryItem gi = new GroceryItem();
			
			gi.setAVALIABLE(total);
			gi.setBUY_MONEY(buyMoney);
			gi.setCAT(cat);
			gi.setNAME(name);
			gi.setSELL_MONEY(sellMoney);
			gi.setTOTAL(total);
			gi.setTYPE(type);
			
			return itemService.addGrocery(gi);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return 0;
		}
	}
}
