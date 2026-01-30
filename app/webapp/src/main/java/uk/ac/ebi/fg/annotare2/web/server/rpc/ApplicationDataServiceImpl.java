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

package uk.ac.ebi.fg.annotare2.web.server.rpc;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fg.annotare2.core.components.Messenger;
import uk.ac.ebi.fg.annotare2.core.data.ProtocolTypes;
import uk.ac.ebi.fg.annotare2.core.data.SystemEfoTerm;
import uk.ac.ebi.fg.annotare2.core.properties.AnnotareProperties;
import uk.ac.ebi.fg.annotare2.magetabcheck.efo.EfoTerm;
import uk.ac.ebi.fg.annotare2.submission.model.ExperimentProfileType;
import uk.ac.ebi.fg.annotare2.submission.model.OntologyTerm;
import uk.ac.ebi.fg.annotare2.web.gwt.common.client.ApplicationDataService;
import uk.ac.ebi.fg.annotare2.web.gwt.common.shared.ApplicationProperties;
import uk.ac.ebi.fg.annotare2.web.gwt.common.shared.ArrayDesignRef;
import uk.ac.ebi.fg.annotare2.web.gwt.common.shared.CookiePopupDeatils;
import uk.ac.ebi.fg.annotare2.web.gwt.common.shared.OntologyTermGroup;
import uk.ac.ebi.fg.annotare2.web.gwt.common.shared.SystemEfoTermMap;
import uk.ac.ebi.fg.annotare2.web.gwt.common.shared.exepriment.ProtocolType;
import uk.ac.ebi.fg.annotare2.web.server.services.AnnotareEfoService;
import uk.ac.ebi.fg.annotare2.web.server.services.ae.ArrayExpress;
import uk.ac.ebi.fg.annotare2.web.server.services.ae.ArrayExpressArrayDesignList;
import uk.ac.ebi.fg.annotare2.web.server.services.ae.ArrayExpressExperimentTypeList;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;
import static uk.ac.ebi.fg.annotare2.web.server.rpc.transform.UIObjectConverter.uiEfoTerm;
import static uk.ac.ebi.fg.annotare2.web.server.rpc.transform.UIObjectConverter.uiEfoTerms;

/**
 * @author Olga Melnichuk
 */
public class ApplicationDataServiceImpl extends ErrorReportingRemoteServiceServlet implements ApplicationDataService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationDataServiceImpl.class);

    private final ArrayExpressArrayDesignList arrayDesignList;
    private final ArrayExpressExperimentTypeList experimentTypeList;

    private final AnnotareEfoService efoService;
    private final AnnotareProperties properties;
    private final ProtocolTypes protocolTypes;

    @Inject
    public ApplicationDataServiceImpl(ArrayExpressArrayDesignList arrayDesignList,
                                      ArrayExpressExperimentTypeList experimentTypeList,
                                      ProtocolTypes protocolTypes,
                                      AnnotareEfoService efoService,
                                      AnnotareProperties properties,
                                      Messenger messenger) {
        super(messenger);
        this.arrayDesignList = arrayDesignList;
        this.experimentTypeList = experimentTypeList;
        this.efoService = efoService;
        this.properties = properties;
        this.protocolTypes = protocolTypes;
    }

    @Override
    public ArrayList<ArrayDesignRef> getArrayDesignList(String query, int limit) {
        return newArrayList(Iterables.transform(Iterables.limit(arrayDesignList.getArrayDesigns(query), limit), new Function<ArrayExpress.ArrayDesign, ArrayDesignRef>() {
            @Nullable
            @Override
            public ArrayDesignRef apply(@Nullable ArrayExpress.ArrayDesign ad) {
                return new ArrayDesignRef(ad.getAccession(), ad.getName());
            }
        }));
    }

    @Override
    public ArrayList<OntologyTerm> getEfoTerms(String query, int limit) {
        return uiEfoTerms(efoService.suggest(query, limit));
    }

    @Override
    public ArrayList<OntologyTerm> getEfoTerms(String query, String rootAccession, int limit) {
        return uiEfoTerms(efoService.suggest(query, rootAccession, limit));
    }

    @Override
    public OntologyTerm getEfoTermByLabel(String label) {
        return uiEfoTerm(efoService.findTermByLabel(label));
    }

    @Override
    public SystemEfoTermMap getSystemEfoTerms() {
        SystemEfoTermMap map = new SystemEfoTermMap();
        for (SystemEfoTerm systemTerm : SystemEfoTerm.values()) {
            String accession = properties.getEfoTermAccession(systemTerm);
            if (accession == null) {
                log.error("application properties do not contain accession for a system term: " + systemTerm);
                continue;
            }

            OntologyTerm term = loadSystemTerm(accession);
            if (term == null) {
                log.error("Unable to load system term by accession: " + accession + "--" + systemTerm);
                continue;
            }

            map.put(systemTerm, term);
        }
        return map;
    }

    @Override
    public ArrayList<ProtocolType> getProtocolTypes(ExperimentProfileType expType) {
        ArrayList<ProtocolType> types = newArrayList();
        for (ProtocolTypes.Config typeConfig : protocolTypes.filter(expType)) {
            EfoTerm term = efoService.findTermByAccession(typeConfig.getId());
            if (term == null) {
                log.error("Protocol Type (" + typeConfig.getId() + ") not found in EFO");
            } else {
                //TODO get definition from the term not from config
                types.add(new ProtocolType(uiEfoTerm(term), typeConfig.getDefinition(), typeConfig.getSubjectType()));
            }
        }
        return types;
    }

    @Override
    public ArrayList<OntologyTerm> getContactRoles() {
        Collection<String> accessions = properties.getContactRoleAccessions();
        ArrayList<OntologyTerm> terms = newArrayList();
        for (String accession : accessions) {
            EfoTerm term = efoService.findTermByAccession(accession);
            if (term == null) {
                log.error("Contact Role (" + accession + ") not found in EFO");
            } else {
                terms.add(uiEfoTerm(term));
            }
        }
        return terms;
    }

    @Override
    public ApplicationProperties getApplicationProperties() {
        return new ApplicationProperties.Builder()
                .setFtpEnabled(properties.isFtpEnabled())
                .setFtpHostname(properties.getPublicFtpHostname())
                .setFtpPath(properties.getPublicFtpPath())
                .setFtpUsername(properties.getPublicFtpUsername())
                .setFtpPassword(properties.getPublicFtpPassword())
                .setAsperaEnabled(properties.isAsperaEnabled())
                .setAsperaUrl(properties.getPublicAsperaUrl())
                .setGlobusEnabled(properties.isGlobusEnabled())
                .setGlobusTransferComponentURL(properties.getGlobusTransferComponentURL())
                .setGlobusTransferAPIURL(properties.getGlobusTransferAPIURL())
                .setContextPath(properties.getContextPath())
                .setSubmitButtonEnabled(properties.isSubmitButtonEnabled())
                .setCreateButtonEnabled(properties.isCreateButtonEnabled())
                .setUploadTutorialUrls(properties.getUploadTutorialLinks())
                .setSamplesTutorialUrls(properties.getSamplesTutorialLinks())
                .setAssignmentTutorialUrls(properties.getAssignmentTutorialLinks())
                .build();
    }

    @Override
    public ArrayList<String> getAeExperimentTypes(ExperimentProfileType type) {
        return new ArrayList<>(experimentTypeList.getExperimentTypes());
    }

    @Override
    public ArrayList<String> getMaterialTypes() {
        return newArrayList(properties.getMaterialTypes());
    }

    @Override
    public ArrayList<String> getSequencingHardware() {
        return newArrayList(properties.getSequencingHardware());
    }

    @Override
    public CookiePopupDeatils getCookiePopupDetails() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(properties.getCookiePopupTemplatePath()));
        String strCurrentLine;
        String[] tokens;
        CookiePopupDeatils cookiePopupDeatils = new CookiePopupDeatils();
        Pattern pattern = Pattern.compile("^notice.*=");
        while ((strCurrentLine = reader.readLine()) != null){
            if(pattern.matcher(strCurrentLine).find()){
                tokens = strCurrentLine.split("=");
                if(tokens[0].endsWith("name")){
                    cookiePopupDeatils.setName(tokens[1]);
                } else if(tokens[0].endsWith("expiryDate")){
                    cookiePopupDeatils.setExpiryDate(Long.parseLong(tokens[1]));
                } else if(tokens[0].endsWith("stopNoticeDate")){
                    cookiePopupDeatils.setStopNoticeDate(Long.parseLong(tokens[1]));
                } else if(tokens[0].endsWith("title")){
                    cookiePopupDeatils.setTitle(tokens[1]);
                } else if(tokens[0].endsWith("html")){
                    cookiePopupDeatils.appendHTML(tokens[1]);
                }
            } else{
                cookiePopupDeatils.appendHTML(strCurrentLine);
            }
        }
        return cookiePopupDeatils;
    }

    @Override
    public ArrayList<OntologyTermGroup> getExperimentalDesigns() {
        Collection<EfoTerm> subTerms = sortedByTermLabel(getChildTerms(SystemEfoTerm.STUDY_DESIGN, 100));

        ArrayList<OntologyTermGroup> groups = newArrayList();
        for (EfoTerm subTerm : subTerms) {
            Collection<EfoTerm> descendants = sortedByTermLabel(efoService.getDescendantTerms(subTerm, 100));

            OntologyTermGroup group = new OntologyTermGroup(subTerm.getLabel());
            for (EfoTerm descendant : descendants) {
                if (descendant.isOrganisational()) {
                    continue;
                }
                group.add(uiEfoTerm(descendant), descendant.getDefinition());
            }
            groups.add(group);
        }
        return groups;
    }

    private Collection<EfoTerm> sortedByTermLabel(Collection<EfoTerm> terms) {
        return Ordering.natural().onResultOf(new Function<EfoTerm, String>() {
            @Override
            public String apply(EfoTerm term) {
                return null != term ? nullToEmpty(term.getLabel()).toLowerCase() : null;
            }
        }).sortedCopy(terms);
    }

    private Collection<String> sorted(Collection<String> terms) {
        return Ordering.natural().onResultOf(new Function<String, String>() {
            @Override
            public String apply(String term) {
                return nullToEmpty(term).toLowerCase();
            }
        }).sortedCopy(terms);
    }

    private Collection<String> transformed(Collection<EfoTerm> terms) {
        return transform(terms, new Function<EfoTerm, String>() {
            @Nullable
            @Override
            public String apply(@Nullable EfoTerm efoTerm) {
                return null != efoTerm ? efoTerm.getLabel() : "";
            }
        });
    }

    private OntologyTerm loadSystemTerm(String accession) {
        return uiEfoTerm(efoService.findTermByAccession(accession));
    }

    private EfoTerm getSystemTerm(SystemEfoTerm systemEfoTerm) {
        EfoTerm sysTerm = efoService.findTermByAccession(properties.getEfoTermAccession(systemEfoTerm));
        if (null == sysTerm) {
            log.error("Unable to find system EFO term: " + systemEfoTerm);
        }
        return sysTerm;
    }

    private Collection<EfoTerm> getChildTerms(SystemEfoTerm baseEfoTerm, int limit) {
        EfoTerm term = getSystemTerm(baseEfoTerm);
        if (null == term) {
            return Collections.emptyList();
        }
        return efoService.getChildTerms(term, limit);
    }

    private Collection<EfoTerm> getDescendantTerms(SystemEfoTerm baseEfoTerm, int limit) {
        EfoTerm term = getSystemTerm(baseEfoTerm);
        if (null == term) {
            return Collections.emptyList();
        }
        return efoService.getDescendantTerms(term, limit);
    }
}
