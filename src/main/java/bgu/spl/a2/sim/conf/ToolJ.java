package bgu.spl.a2.sim.conf;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ToolJ {
	
	/**
	 * This class is used to parse the settings for the warehouse iventory from a Json file
	 */
	
	@SerializedName("tool")
	@Expose
	private String tool;
	@SerializedName("qty")
	@Expose
	private Integer qty;

	public String getTool() {
		return tool;
	}

	public void setTool(String tool) {
		this.tool = tool;
	}

	public Integer getQty() {
		return qty;
	}

	public void setQty(Integer qty) {
		this.qty = qty;
	}

}