/*
 * Copyright 2009-2016 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.fg.annotare2.core.magetab;

import uk.ac.ebi.fg.annotare2.magetabcheck.checker.CheckResult;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Olga Melnichuk
 */
public class MageTabUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static String formatDate(Date date) {
        return date == null ? "" : DATE_FORMAT.format(date);
    }

    // fix date that's entered in a different time zone (by aligning it to a closer midnight GMT
    // will work incorrectly for those living in GMT-12 and GMT+13,+14
    public static Date fixDate(Date date) {
        if (null == date) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        if (0 == hours) {
            return date;
        } else if (hours < 12) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            return cal.getTime();
        } else {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.add(Calendar.DATE, 1);
            return cal.getTime();
        }
    }

    public static boolean isObsoleteError(String code) {
        return obsoleteErrors.contains(code);
    }

    public static boolean isObsoleteWarning(String code) { return obsoleteWarnings.contains(code); }

    public static String getErrorString(CheckResult cr) {
        String ref = cr.getReference();
        if (!errorMap.containsKey(ref)) return cr.getTitle();
        StringBuilder sb = new StringBuilder(errorMap.get(ref));
        if (cr.getDynamicDetail()!=null) {
            return String.format(sb.toString(), cr.getDynamicDetail() );
        }
        return sb.toString();
    }

    public static String getWarningString(CheckResult cr) {
        String ref = cr.getReference();
        if(!warningMap.containsKey(ref)) return cr.getTitle();
        StringBuilder sb = new StringBuilder(warningMap.get(ref));
        if(cr.getDynamicDetail() != null) {
            return String.format(sb.toString(), cr.getDynamicDetail());
        }
        return sb.toString();
    }

    private static final Map<String, String> errorMap = new HashMap<>();
    private static final HashSet obsoleteErrors = new HashSet<>();
    private static final Map<String, String> warningMap = new HashMap<>();
    private static final HashSet obsoleteWarnings = new HashSet<>();
    static {
        errorMap.put("ADMN01", "[<a href=\"#DESIGN:FILES\">Assign Files</a>] The 'Raw Data Matrix File' column has not been filled in (completely). See <a target=\"_blank\" href=\"../../help/file_upload.html#assign-files\">Assign files to samples (Annotare Help)</a> for more details.");
        errorMap.put("ADMN02", "[<a href=\"#DESIGN:FILES\">Assign Files</a>] An array data matrix file name can only contain alphanumeric characters, underscores and dots. Click on the file name in the upload panel to rename, then press 'Enter' to save the changes.");
        errorMap.put("ADN01", "[<a href=\"#DESIGN:FILES\">Assign Files</a>] The 'Raw Data File' column has not been filled in (completely). See <a target=\"_blank\" href=\"../../help/file_upload.html#assign-files\">Assign files to samples (Annotare Help)</a> for more details.");
        errorMap.put("ADN02", "[<a href=\"#DESIGN:FILES\">Assign Files</a>] (%s) A raw data file name can not contain spaces or special characters (except '_', '-', '.', '#'). Click on the file name in the upload panel to rename, then press 'Enter' to save the changes.");
        errorMap.put("AN01", "[<a href=\"#DESIGN:FILES\">Assign Files</a>] An assay name could not be generated automatically. You may not have assigned a data file to the sample(s). See <a target=\"_blank\" href=\"../../help/file_upload.html#assign-files\">Assign files to samples (Annotare Help)</a> for more details.");
        errorMap.put("AN08", "[<a href=\"#DESIGN:FILES\">Assign Files</a>] %s. You may have chosen the wrong type of data column (e.g. choose 'Raw Matrix', not 'Raw', if you have 1 file containing raw data for multiple samples). For two-color experiments, there may be a problem with the relationship between files and samples (expects 1 raw data file per 2 labeled extracts). See <a target=\"_blank\" href=\"../../help/file_upload.html#assign-files\">Assign files to samples (Annotare Help)</a> and <a target=\"_blank\" href=\"../../help/two_color_ma.html\">Two-color experiments (Annotare Help)</a> for more details.");
        errorMap.put("AF01", "[<a href=\"#DESIGN:FILES\">Assign Files</a>] (%s) One file should only be assigned only to one sample.");
        errorMap.put("DADMN01", "[<a href=\"#DESIGN:FILES\">Assign Files</a>] A processed data matrix file name can only contain alphanumeric characters, underscores and dots. Click on the file name in the upload panel to rename, then press 'Enter' to save the changes.");
        errorMap.put("DADMN02", "[<a href=\"#DESIGN:FILES\">Assign Files</a>] (%s) A processed data matrix file name can only contain alphanumeric characters, underscores and dots. Click on the file name in the upload panel to rename, then press 'Enter' to save the changes.");
        errorMap.put("DADN02", "[<a href=\"#DESIGN:FILES\">Assign Files</a>] (%s) A processed data file name can not contain spaces or special characters (except '_', '-', '.', '#'). Click on the file name in the upload panel to rename, then press 'Enter' to save the changes.");
        errorMap.put("AN03", "[<a href=\"#DESIGN:PROTOCOLS\">Protocols</a>] A 'nucleic acid sequencing protocol' must be included. To create a protocol, go to [<a href=\"#DESIGN:PROTOCOLS\">Describe protocols</a>] and press the 'Add Protocol' button. Insert text in the 'Description' field for the required protocol.");
        errorMap.put("AN04", "[<a href=\"#DESIGN:PROTOCOLS\">Protocols</a>] A 'nucleic acid hybridization to array protocol' must be included. To create a protocol, go to [<a href=\"#DESIGN:PROTOCOLS\">Describe protocols</a>] and press the 'Add Protocol' button. Insert text in the 'Description' field for the required protocol.");
        errorMap.put("C01", "[<a href=\"#DESIGN:CONTACTS\">Contacts</a>] At least one contact must be included. Go to [<a href=\"#DESIGN:CONTACTS\">Contacts</a>] and press the '+' sign to create a new contact.");
        errorMap.put("C02", "[<a href=\"#DESIGN:CONTACTS\">Contacts</a>] A contact must have a last name specified. Go to [<a href=\"#DESIGN:CONTACTS\">Contacts</a>], select a contact, and fill in the form.");
        errorMap.put("C03", "[<a href=\"#DESIGN:CONTACTS\">Contacts</a>] At least one contact must have an email address specified. Go to [<a href=\"#DESIGN:CONTACTS\">Contacts</a>], select a contact, and fill in the form.");
        errorMap.put("C04", "[<a href=\"#DESIGN:CONTACTS\">Contacts</a>] At least one contact must have a role specified. In the [<a href=\"#DESIGN:CONTACTS\">Contacts</a>] form, click on 'change' next to 'Roles' to select the role(s) of the contact.");
        errorMap.put("C05", "[<a href=\"#DESIGN:CONTACTS\">Contacts</a>] At least one contact must have the role 'submitter'. In the [<a href=\"#DESIGN:CONTACTS\">Contacts</a>] form, click on 'change' next to 'Roles' to select the role(s) of the contact.");
        errorMap.put("C06", "[<a href=\"#DESIGN:CONTACTS\">Contacts</a>] A contact with 'submitter' role must have an affiliation specified. Go to [<a href=\"#DESIGN:CONTACTS\">Contacts</a>], select the contact with role 'submitter', and fill in the form.");
        errorMap.put("C07", "[<a href=\"#DESIGN:CONTACTS\">Contacts</a>] A contact with 'submitter' role must have an email address specified. Go to [<a href=\"#DESIGN:CONTACTS\">Contacts</a>], select the contact with role 'submitter', and fill in the form.");
        errorMap.put("DADMN04", "[<a href=\"#DESIGN:PROTOCOLS\">Protocols</a>] A 'normalization data transformation protocol' must be included which describes the analysis methods used to generate the processed data matrix file. To create a protocol, go to [<a href=\"#DESIGN:PROTOCOLS\">Describe protocols</a>] and press the 'Add Protocol' button. Insert text in the 'Description' field for the required protocol.");
        errorMap.put("DADN04", "[<a href=\"#DESIGN:PROTOCOLS\">Protocols</a>] A 'normalization data transformation protocol' must be included which describes the analysis methods used to generate the processed data file(s). To create a protocol, go to [<a href=\"#DESIGN:PROTOCOLS\">Describe protocols</a>] and press the 'Add Protocol' button. Insert text in the 'Description' field for the required protocol.");
        errorMap.put("EF01", "[<a href=\"#DESIGN:SAMPLES\">Sample annotation</a>] An experiment must have at least one experimental variable. Go to [<a href=\"#DESIGN:SAMPLES\">Samples</a>] and press 'Add Sample Attributes and Variables...'. In the left panel, select the sample attribute that varies among your samples and is subject of the study, then tick the box next to 'Experimental Variable'.");
        errorMap.put("EX03", "[<a href=\"#DESIGN:PROTOCOLS\">Protocols</a>] A 'nucleic acid extraction protocol' must be included. To create a protocol, go to [<a href=\"#DESIGN:PROTOCOLS\">Describe protocols</a>] and press the 'Add Protocol' button. Insert text in the 'Description' field for the required protocol.");
        errorMap.put("EX04", "[<a href=\"#DESIGN:PROTOCOLS\">Protocols</a>] A 'nucleic acid library construction protocol' must be included. To create a protocol, go to [<a href=\"#DESIGN:PROTOCOLS\">Describe protocols</a>] and press the 'Add Protocol' button. Insert text in the 'Description' field for the required protocol.");
        errorMap.put("FV04", "[<a href=\"#DESIGN:SAMPLES\">Sample annotation</a>] (%s) The values of an experimental variable must vary (for compound+dose at least one must vary). One of the experimental variables contains the same values for all samples. Make sure the correct values were entered, or if the attribute is not a factor in this experiment, uncheck 'Experimental Variable' in the 'Add Samples and Experimental Variables' dialogue.");
        errorMap.put("G01", "[<a href=\"#DESIGN:GENERAL_INFO\">General Info</a>] The experiment must have a title. Please provide an informative title that states the intent (and not conclusion) of the experiment.");
        errorMap.put("G02", "[<a href=\"#DESIGN:GENERAL_INFO\">General Info</a>] The experiment must have a description. Please outline the scientific background and the experimental workflow in the text field.");
        errorMap.put("G05", "[<a href=\"#DESIGN:GENERAL_INFO\">General Info</a>] A public release date for the experiment must be entered. To keep the experiment in private state after submission, select a date up to 1 year in the future. This can be changed later.");
        errorMap.put("LC01", "[<a href=\"#DESIGN:EXTRACTS_LIBRARY_INFO\">Library Info</a>] The library source, layout, selection and strategy must be specified in the ENA library info. See <a target=\"_blank\" href=\"../../help/seq_lib_spec.html\">Sequencing library information (Annotare Help)</a> for more details.");
        errorMap.put("LC02", "[<a href=\"#DESIGN:EXTRACTS_LIBRARY_INFO\">Library Info</a>] NOMINAL_LENGTH and NOMINAL_SDEV must be provided for paired-end sequencing samples in [<a href=\"#DESIGN:EXTRACTS_LIBRARY_INFO\">Library Info</a>]. Both must be a positive integer. See <a target=\"_blank\" href=\"../../help/seq_lib_spec.html\">Sequencing library information (Annotare Help)</a> for more details.");
        errorMap.put("SC01", "[<a href=\"#DESIGN:SINGLE_CELL_LIBRARY_INFO\">Single-cell Library Info</a>] The 'library construction' and 'spike in' must be specified for all samples. See <a target=\"_blank\" href=\"../../help/single-cell_submission_guide.html#LibraryInfo\">Single-cell submission guide (Annotare Help)</a> for more details.");
        errorMap.put("LE05", "[<a href=\"#DESIGN:PROTOCOLS\">Protocols</a>] A 'nucleic acid labeling protocol' must be included. To create a protocol, go to [<a href=\"#DESIGN:PROTOCOLS\">Describe protocols</a>] and press the 'Add Protocol' button. Insert text in the 'Description' field for the required protocol.");
        errorMap.put("PB02", "[<a href=\"#DESIGN:PUBLICATIONS\">Publication</a>] The PubMed ID field can only contain numbers, and no other characters (including whitespaces).");
        errorMap.put("PN06", "[<a href=\"#DESIGN:PROTOCOLS\">Protocols</a>] For the 'nucleic acid sequencing protocol' a 'performer' must be specified. Enter the name of the institute or sequencing center that generated the sequencing files under 'Performer' for the 'nucleic acid sequencing protocol'.");
        errorMap.put("PR01", "[<a href=\"#DESIGN:PROTOCOLS\">Protocols</a>] At least one protocol must be used in an experiment. To create a protocol, go to [<a href=\"#DESIGN:PROTOCOLS\">Describe protocols</a>] and press the 'Add Protocol' button. Insert text in the 'Description' field for the required protocol.");
        errorMap.put("PR05", "[<a href=\"#DESIGN:PROTOCOLS\">Protocols</a>] A protocol needs to have a description. Enter (or paste) a protocol text in the 'Description' field.");
        errorMap.put("PR08", "[<a href=\"#DESIGN:PROTOCOLS\">Protocols</a>] A 'nucleic acid library construction protocol' must be included for sequencing submissions. To create a protocol, go to [<a href=\"#DESIGN:PROTOCOLS\">Describe protocols</a>] and press the 'Add Protocol' button. Insert text in the 'Description' field for the required protocol.");
        errorMap.put("PR09", "[<a href=\"#DESIGN:PROTOCOLS\">Protocols</a>] A 'nucleic acid sequencing protocol' must be included for sequencing submissions. To create a protocol, go to [<a href=\"#DESIGN:PROTOCOLS\">Describe protocols</a>] and press the 'Add Protocol' button. Insert text in the 'Description' field for the required protocol.");
        errorMap.put("PR10", "[<a href=\"#DESIGN:PROTOCOLS\">Protocols</a>] The 'nucleic acid sequencing protocol' must specify the sequencing hardware. Choose the name of the sequencing machine from the drop-down menu under 'Hardware' for the 'nucleic acid sequencing protocol'.");
        errorMap.put("SR04", "[<a href=\"#DESIGN:SAMPLES\">Sample annotation</a>] 'Organism' is a mandatory attribute for the source material and must be filled in.");
        errorMap.put("SR08", "[<a href=\"#DESIGN:PROTOCOLS\">Protocols</a>] (%s) A 'growth protocol', 'treatment protocol' or 'sample collection protocol' must be included. To create a protocol, go to [<a href=\"#DESIGN:PROTOCOLS\">Describe protocols</a>] and press the 'Add Protocol' button. Insert text in the 'Description' field for the required protocol.");

        warningMap.put("C08", "[<a href=\"#DESIGN:CONTACTS\">Contacts</a>] A contact should have first name specified");
        warningMap.put("UN01", "[<a href=\"#DESIGN:SAMPLES\">Sample annotation</a>] Attribute 'Age' should have a unit specified. Go to 'Add Sample Attributes and Variables', select the attribute/variable and enter the measurement unit.");
        warningMap.put("UN02", "[<a href=\"#DESIGN:SAMPLES\">Sample annotation</a>] Experimental variable 'Time' should have a unit specified. Go to 'Add Sample Attributes and Variables', select the attribute/variable and enter the measurement unit.");
        warningMap.put("UN03", "[<a href=\"#DESIGN:SAMPLES\">Sample annotation</a>] Experimental variable 'Dose' should have a unit specified. Go to 'Add Sample Attributes and Variables', select the attribute/variable and enter the measurement unit.");

        obsoleteErrors.add("ADMN03");
        obsoleteErrors.add("DADMN03");
        obsoleteErrors.add("DADN03");
        obsoleteErrors.add("DADN01");
        obsoleteErrors.add("ADN03");
        obsoleteErrors.add("AD01");
        obsoleteErrors.add("AD03");
        obsoleteErrors.add("AD04");
        obsoleteErrors.add("AN02");
        obsoleteErrors.add("AN03");
        obsoleteErrors.add("AN05");
        obsoleteErrors.add("AN06");
        obsoleteErrors.add("CA03");
        obsoleteErrors.add("COM01");
        obsoleteErrors.add("EF02");
        obsoleteErrors.add("EX01");
        obsoleteErrors.add("FV03");
        obsoleteErrors.add("G04");
        obsoleteErrors.add("G06");
        obsoleteErrors.add("G07");
        obsoleteErrors.add("G08");
        obsoleteErrors.add("G09");
        obsoleteErrors.add("L03");
        obsoleteErrors.add("LE01");
        obsoleteErrors.add("LE02");
        obsoleteErrors.add("LE04");
        obsoleteErrors.add("MT03");
        obsoleteErrors.add("PN03");
        obsoleteErrors.add("PN05");
        obsoleteErrors.add("PR03");
        obsoleteErrors.add("SC02");
        obsoleteErrors.add("SM01");
        obsoleteErrors.add("SR01");
        obsoleteErrors.add("SR06");
        obsoleteErrors.add("TS01");
        obsoleteErrors.add("TS02");
        obsoleteErrors.add("TT01");
        obsoleteErrors.add("TT03");
        obsoleteErrors.add("UA03");
        obsoleteErrors.add("PN01");
        obsoleteErrors.add("PR02");

        obsoleteWarnings.add("AD02");
        obsoleteWarnings.add("ADMN04");
        obsoleteWarnings.add("ADN04");
        obsoleteWarnings.add("C09");
        obsoleteWarnings.add("C10");
        obsoleteWarnings.add("CA01");
        obsoleteWarnings.add("CA02");
        obsoleteWarnings.add("ED01");
        obsoleteWarnings.add("ED02");
        obsoleteWarnings.add("ED03");
        obsoleteWarnings.add("EF03");
        obsoleteWarnings.add("EF04");
        obsoleteWarnings.add("EX02");
        obsoleteWarnings.add("FV01");
        obsoleteWarnings.add("FV02");
        obsoleteWarnings.add("G03");
        obsoleteWarnings.add("G09");
        obsoleteWarnings.add("L01");
        obsoleteWarnings.add("L02");
        obsoleteWarnings.add("LE03");
        obsoleteWarnings.add("MT01");
        obsoleteWarnings.add("MT02");
        obsoleteWarnings.add("NN01");
        obsoleteWarnings.add("NT01");
        obsoleteWarnings.add("NT02");
        obsoleteWarnings.add("NT03");
        obsoleteWarnings.add("PB01");
        obsoleteWarnings.add("PB03");
        obsoleteWarnings.add("PB04");
        obsoleteWarnings.add("PB05");
        obsoleteWarnings.add("PB06");
        obsoleteWarnings.add("PN04");
        obsoleteWarnings.add("PN07");
        obsoleteWarnings.add("PR04");
        obsoleteWarnings.add("PR06");
        obsoleteWarnings.add("PR07");
        obsoleteWarnings.add("PV01");
        obsoleteWarnings.add("PV02");
        obsoleteWarnings.add("QC01");
        obsoleteWarnings.add("QC02");
        obsoleteWarnings.add("QC03");
        obsoleteWarnings.add("RT01");
        obsoleteWarnings.add("RT02");
        obsoleteWarnings.add("RT03");
        obsoleteWarnings.add("SM02");
        obsoleteWarnings.add("SM03");
        obsoleteWarnings.add("SR03");
        obsoleteWarnings.add("SR05");
        obsoleteWarnings.add("TS03");
        obsoleteWarnings.add("TS04");
        obsoleteWarnings.add("TT02");
        obsoleteWarnings.add("UA01");
        obsoleteWarnings.add("UA02");
        obsoleteWarnings.add("SR02");
        obsoleteWarnings.add("REF01");
        obsoleteWarnings.add("DF01");

    }
}
