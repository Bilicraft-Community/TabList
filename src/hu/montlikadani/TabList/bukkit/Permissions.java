package hu.montlikadani.tablist;

public class Permissions {

	public enum Perm {
		ADDFAKEPLAYER("tablist.fakeplayers.add"),
		FAKEPLAYERS("tablist.fakeplayers"),
		GET("tablist.get"),
		GETO("tablist.get.other"),
		HELP("tablist.help"),
		LISTFAKEPLAYERS("tablist.fakeplayers.list"),
		RELOAD("tablist.reload"),
		REMOVEFAKEPLAYER("tablist.fakeplayers.remove"),
		RESET("tablist.reset"),
		RESETOTHERTAB("tablist.reset.other"),
		SETPREFIX("tablist.setprefix"),
		SETPRIORITY("tablist.setpriority"),
		SETSUFFIX("tablist.setsuffix"),
		REMOVEPLAYER("tablist.removeplayer"),
		TABNAME("tablist.tabname"),
		TABNAMEOTHER("tablist.tabname.other"),
		TOGGLE("tablist.toggle"),
		TOGGLEALL("tablist.toggle.all");

		private String perm;

		Perm(String perm) {
			this.perm = perm;
		}

		public String getPerm() {
			return perm;
		}
	}
}
