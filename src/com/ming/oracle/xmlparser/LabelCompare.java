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

		SGUIDValidator srcsvad = new SGUIDValidator(lsrcfileName);
		srcsvad.setBranchName(args[2]);
		SGUIDValidator trgsvad = new SGUIDValidator(ltrgfileName);
		trgsvad.setBranchName(args[3]);
		srcsvad.compare(trgsvad);
	}
}