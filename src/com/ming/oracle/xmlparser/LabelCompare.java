package com.ming.oracle.xmlparser;

public class LabelCompare {
	/**
	 * First it will parse all files within the transaction.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 4) {
			System.err.println("Usages:LabelCompare RefNameSource RefNameTarget SourceBranchName TargetBranchName");
			System.exit(2);
		}

		String lsrcfileName = args[0];
		String ltrgfileName = args[1];

		String [] lfields = args[2].split("\\|");
		SGUIDValidator srcsvad = new SGUIDValidator(lsrcfileName);
		srcsvad.setBranchName(lfields[0]);
		srcsvad.setLabel(lfields[1]);
		lfields = args[3].split("\\|");
		SGUIDValidator trgsvad = new SGUIDValidator(ltrgfileName);
		trgsvad.setBranchName(lfields[0]);
		trgsvad.setLabel(lfields[1]);
		srcsvad.compare(trgsvad);
	}
}