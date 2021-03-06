/* This library is under the 3-Clause BSD License

Copyright (c) 2018-2020, Orange S.A.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright notice,
     this list of conditions and the following disclaimer in the documentation
     and/or other materials provided with the distribution.

  3. Neither the name of the copyright holder nor the names of its contributors
     may be used to endorse or promote products derived from this software without
     specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 @author Johannes Heinecke
 @version 2.4.3 as of 21st May 2020
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orange.labs.editor.ConlluEditor;
import com.orange.labs.conllparser.ConllException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestConlluEditor {

    ConlluEditor ce;
    File folder;


    @Before
    public void setUp() throws ConllException, IOException {
        URL url = this.getClass().getResource("test.conllu");
        File file = new File(url.getFile());
        try {
            ce = new ConlluEditor(file.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        folder = new File("testoutput");
        folder.mkdir();
    }

    //@Rule
    //public TemporaryFolder folder = new TemporaryFolder();

    private void name(String n) {
        System.out.format("\n***** Testing: %S ****\n", n);
    }

    @Test
    public void test01Latex() throws IOException {
        name("LaTeX output");

        //System.out.println("=================== " + folder.getRoot());
        //File out = folder.newFile("test.tex");
        File out = new File(folder,  "test.tex");

        URL url = this.getClass().getResource("test.tex");

        // call process to be sure makeTrees has been  called
        //String rtc = 
        ce.process("read 13", 1, "editinfo");
        String res = ce.getraw(ConlluEditor.Raw.LATEX, 13);
        // first sentence 13
        JsonElement jelement = JsonParser.parseString(res);  //new JsonParser().parse(res);
        JsonObject jobject = jelement.getAsJsonObject();

        StringBuilder sb = new StringBuilder();
        sb.append(jobject.get("raw").getAsString()).append('\n');

        // add LateX for sentence 18
        res = ce.getraw(ConlluEditor.Raw.LATEX, 18);
        jelement = JsonParser.parseString(res); 
        jobject = jelement.getAsJsonObject();
        sb.append(jobject.get("raw").getAsString()).append('\n');

        FileUtils.writeStringToFile(out, sb.toString(), //jobject.get("raw").getAsString(),
                                         StandardCharsets.UTF_8);

        Assert.assertEquals(String.format("LaTeX output incorrect\n ref: %s\n res: %s\n", url.toString(), out.toString()),
                FileUtils.readFileToString(new File(url.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(out, StandardCharsets.UTF_8));
    }

    @Test
    public void test02Json() throws IOException {
        name("JSON output");

        //System.out.println("=================== " + folder.getRoot());
        File out = new File(folder,  "test.json");

        URL url = this.getClass().getResource("test.json");

        // call proces to be sure makeTrees has been  called
        //String rtc = 
        ce.process("read 13", 1, "editinfo");
        String res = ce.getraw(ConlluEditor.Raw.SPACY_JSON, 13);
        JsonElement jelement = JsonParser.parseString(res); 
        JsonObject jobject = jelement.getAsJsonObject();

        FileUtils.writeStringToFile(out, jobject.get("raw").getAsString(), StandardCharsets.UTF_8);

        Assert.assertEquals(String.format("JSON output incorrect\n ref: %s\n res: %s\n", url.toString(), out.toString()),
                FileUtils.readFileToString(new File(url.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(out, StandardCharsets.UTF_8));
    }

    @Test
    public void test03Conllu() throws IOException {
        name("CoNLL-U output");

        //File out = folder.newFile("out1.conllu");
        File out = new File(folder, "out1.conllu");

        URL url = this.getClass().getResource("out1.conllu");

        String res = ce.getraw(ConlluEditor.Raw.CONLLU, 13);
        JsonElement jelement = JsonParser.parseString(res); 
        JsonObject jobject = jelement.getAsJsonObject();
        FileUtils.writeStringToFile(out, jobject.get("raw").getAsString(), StandardCharsets.UTF_8);

        //String rtc =
        ce.process("read 14", 1, "editinfo");
        res = ce.getraw(ConlluEditor.Raw.CONLLU, 14);
        jelement = JsonParser.parseString(res); 
        jobject = jelement.getAsJsonObject();
        FileUtils.writeStringToFile(out, jobject.get("raw").getAsString(), StandardCharsets.UTF_8, true);

        //rtc = 
        ce.process("read 15", 1, "editinfo");
        res = ce.getraw(ConlluEditor.Raw.CONLLU, 15);
        jelement = JsonParser.parseString(res); 
        jobject = jelement.getAsJsonObject();
        FileUtils.writeStringToFile(out, jobject.get("raw").getAsString(), StandardCharsets.UTF_8, true);

        Assert.assertEquals(String.format("CoNLL-U output incorrect\n ref: %s\n res: %s\n", url.toString(), out.toString()),
                FileUtils.readFileToString(new File(url.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(out, StandardCharsets.UTF_8));

    }

    @Test
    public void test11EditJoinSplit() throws IOException {
        name("modifying lemma, deprel, and split/join");
        ce.setCallcitcommot(false);
        //String rtc = 
        ce.process("mod lemma 3 Oasis", 3, "editinfo");
        /*rtc = */ce.process("mod 1 2 detfalse", 3, "editinfo");
        ce.process("mod split 19", 3, "editinfo");
        ce.process("mod join 5", 3, "editinfo");

        URL ref = this.getClass().getResource("test.mod.conllu");
        URL res = this.getClass().getResource("test.conllu.2"); // modified file
        Assert.assertEquals(String.format("CoNLL-U output incorrect\n ref: %s\n res: %s\n", ref.toString(), res.toString()),
                FileUtils.readFileToString(new File(ref.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(new File(res.getFile()), StandardCharsets.UTF_8));
    }

    @Test
    public void test12EditJoinSplitBeforeMWT() throws IOException {
        name("split/join before a MWT");
        ce.setCallcitcommot(false);
        ce.setBacksuffix(".5");
        ce.process("mod split 2 3", 4, "editinfo");
        ce.process("mod join 5", 4, "editinfo");

        URL ref = this.getClass().getResource("test.split-mwt.conllu");
        URL res = this.getClass().getResource("test.conllu.5"); // modified file
        Assert.assertEquals(String.format("CoNLL-U output incorrect\n ref: %s\n res: %s\n", ref.toString(), res.toString()),
                FileUtils.readFileToString(new File(ref.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(new File(res.getFile()), StandardCharsets.UTF_8));
    }

    @Test
    public void test13EditJoinSplitBeforeEmptyNode() throws IOException {
        name("split/join before a empty word");
        ce.setCallcitcommot(false);
        ce.setBacksuffix(".6");
        //String rtc = 
        ce.process("mod split 5 ", 16, "editinfo");
        ce.process("mod join 13", 16, "editinfo");

        URL ref = this.getClass().getResource("test.split-ew.conllu");
        URL res = this.getClass().getResource("test.conllu.6"); // modified file
        Assert.assertEquals(String.format("CoNLL-U output incorrect\n ref: %s\n res: %s\n", ref.toString(), res.toString()),
                FileUtils.readFileToString(new File(ref.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(new File(res.getFile()), StandardCharsets.UTF_8));
    }

    @Test
    public void test14EditJoinSplitWithEnhDeps() throws IOException {
        name("split/join before a enhanced deps");
        ce.setCallcitcommot(false);
        ce.setBacksuffix(".7");
        ce.process("mod split 4 ", 13, "editinfo");
        ce.process("mod split 3", 13, "editinfo");
        ce.process("mod join 4", 13, "editinfo");

        URL ref = this.getClass().getResource("test.split-ed.conllu");
        URL res = this.getClass().getResource("test.conllu.7"); // modified file
        Assert.assertEquals(String.format("CoNLL-U output incorrect\n ref: %s\n res: %s\n", ref.toString(), res.toString()),
                FileUtils.readFileToString(new File(ref.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(new File(res.getFile()), StandardCharsets.UTF_8));
    }

    @Test
    public void test15CreateMWT() throws IOException {
        name("create two MWT with three/two words and rename contracted form");
        ce.setCallcitcommot(false);
        ce.setBacksuffix(".8");
        ce.process("mod compose 1 3", 17, "editinfo");
        ce.process("mod editmwe 1 3 Dáselle", 17, "editinfo");

        ce.process("mod compose 6 2", 17, "editinfo");
        ce.process("mod editmwe 6 7 ao Gloss=to_him", 17, "editinfo");

        URL ref = this.getClass().getResource("test.create-mwt.conllu");
        URL res = this.getClass().getResource("test.conllu.8"); // modified file
        Assert.assertEquals(String.format("CoNLL-U output incorrect\n ref: %s\n res: %s\n", ref.toString(), res.toString()),
                FileUtils.readFileToString(new File(ref.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(new File(res.getFile()), StandardCharsets.UTF_8));
    }

    @Test
    public void test16JoinOverlapMWTstart() throws IOException {
        name("join overlapping be first word of a MWT");
        ce.setCallcitcommot(false);
        ce.setBacksuffix(".9");
        //String rtc = 
        ce.process("mod join 5", 6, "editinfo");

        URL ref = this.getClass().getResource("test.join-mwt.conllu");
        URL res = this.getClass().getResource("test.conllu.9"); // modified file
        Assert.assertEquals(String.format("CoNLL-U output incorrect\n ref: %s\n res: %s\n", ref.toString(), res.toString()),
                FileUtils.readFileToString(new File(ref.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(new File(res.getFile()), StandardCharsets.UTF_8));
    }

    @Test
    public void test17JoinOverlapMWTend() throws IOException {
        name("join overlapping be last word of a MWT");
        ce.setCallcitcommot(false);
        ce.setBacksuffix(".10");
        //String rtc = 
        ce.process("mod join 6", 6, "editinfo");

        URL ref = this.getClass().getResource("test.join-mwt-2.conllu");
        URL res = this.getClass().getResource("test.conllu.10"); // modified file
        Assert.assertEquals(String.format("CoNLL-U output incorrect\n ref: %s\n res: %s\n", ref.toString(), res.toString()),
                FileUtils.readFileToString(new File(ref.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(new File(res.getFile()), StandardCharsets.UTF_8));
    }

    @Test
    public void test21Read() throws IOException {
        name("read sentence");
        ce.setCallcitcommot(false);
        String rtc = ce.process("read 13", 1, "");
        JsonElement jelement = JsonParser.parseString(rtc); 

        //File out = folder.newFile("read.json");
        File out = new File(folder, "read.json");
        //System.err.println(prettyprintJSON(jelement));
        FileUtils.writeStringToFile(out, prettyprintJSON(jelement), StandardCharsets.UTF_8);

        URL ref = this.getClass().getResource("read.json");

        Assert.assertEquals(String.format("Read return incorrect\n ref: %s\n res: %s\n", ref.toString(), out.toString()),
                FileUtils.readFileToString(new File(ref.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(out, StandardCharsets.UTF_8));
    }

    @Test
    public void test22ReadSecond() throws IOException {
        name("read a second sentence");
        ce.setCallcitcommot(false);
        String rtc = ce.process("read 16", 1, "");
        JsonElement jelement = JsonParser.parseString(rtc); 

        //File out = folder.newFile("read.json");
        File out = new File(folder, "read_16.json");
        //System.err.println(prettyprintJSON(jelement));
        FileUtils.writeStringToFile(out, prettyprintJSON(jelement), StandardCharsets.UTF_8);

        URL ref = this.getClass().getResource("read_16.json");

        Assert.assertEquals(String.format("read return code incorrect\n ref: %s\n res: %s\n", ref.toString(), out.toString()),
                FileUtils.readFileToString(new File(ref.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(out, StandardCharsets.UTF_8));
    }

    @Test
    public void test31FindLemma() throws IOException {
        name("findlemma");
        ce.setCallcitcommot(false);
        String rtc = ce.process("findlemma false fromage/.*/puer", 1, "");
        JsonElement jelement = JsonParser.parseString(rtc); 

        //File out = folder.newFile("findlemma.json");
        File out = new File(folder, "findlemma.json");

        //System.err.println(prettyprintJSON(jelement));
        FileUtils.writeStringToFile(out, prettyprintJSON(jelement), StandardCharsets.UTF_8);

        URL ref = this.getClass().getResource("findlemma.json");

        Assert.assertEquals(String.format("Find lemma return incorrect\n ref: %s\n res: %s\n", ref.toString(), out.toString()),
                FileUtils.readFileToString(new File(ref.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(out, StandardCharsets.UTF_8));
    }

    @Test
    public void test32FindForm() throws IOException {
        name("findword");
        ce.setCallcitcommot(false);
        String rtc = ce.process("findword false \" and \"", 1, "");
        JsonElement jelement = JsonParser.parseString(rtc); 

        //File out = folder.newFile("findform.json");
        File out = new File(folder, "findform.json");

        //System.err.println(prettyprintJSON(jelement));
        FileUtils.writeStringToFile(out, prettyprintJSON(jelement), StandardCharsets.UTF_8);

        URL ref = this.getClass().getResource("findform.json");

        Assert.assertEquals(String.format("Find form return incorrect\n ref: %s\n res: %s\n", ref.toString(), out.toString()),
                FileUtils.readFileToString(new File(ref.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(out, StandardCharsets.UTF_8));
    }

    @Test
    public void test33FindSentenceId() throws IOException {
        name("findsenteid");
        ce.setCallcitcommot(false);
        String rtc = ce.process("findsentid false c.*-ud", 1, "");
        JsonElement jelement = JsonParser.parseString(rtc); 

        //File out = folder.newFile("findform.json");
        File out = new File(folder, "findsentid.json");

        //System.err.println(prettyprintJSON(jelement));
        FileUtils.writeStringToFile(out, prettyprintJSON(jelement), StandardCharsets.UTF_8);

        URL ref = this.getClass().getResource("findsentid.json");

        Assert.assertEquals(String.format("Find form return incorrect\n ref: %s\n res: %s\n", ref.toString(), out.toString()),
                FileUtils.readFileToString(new File(ref.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(out, StandardCharsets.UTF_8));
    }

    @Test
    public void test34FindSentenceIdBadRE() throws IOException {
        name("findsenteid (bad RE)");
        ce.setCallcitcommot(false);
        String rtc = ce.process("findsentid false c.*[]-ud", 1, "");
        JsonElement jelement = JsonParser.parseString(rtc); 

        //File out = folder.newFile("findform.json");
        File out = new File(folder, "findsentidBadRE.json");

        //System.err.println(prettyprintJSON(jelement));
        // delete CR (\r) otherwise this tests fails on Windows...
        FileUtils.writeStringToFile(out, prettyprintJSON(jelement).replaceAll("\\\\r", ""), StandardCharsets.UTF_8);

        URL ref = this.getClass().getResource("findsentidBadRE.json");

        Assert.assertEquals(String.format("Find form return incorrect\n ref: %s\n res: %s\n", ref.toString(), out.toString()),
                FileUtils.readFileToString(new File(ref.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(out, StandardCharsets.UTF_8));
    }

    @Test
    public void test35FindNothing() throws IOException {
        name("findupos (error)");
        ce.setCallcitcommot(false);
        String rtc = ce.process("findupos false TOTO", 1, "");
        JsonElement jelement = JsonParser.parseString(rtc); 

        //File out = folder.newFile("finduposKO.json");
        File out = new File(folder, "finduposKO.json");

        //System.err.println(prettyprintJSON(jelement));
        FileUtils.writeStringToFile(out, prettyprintJSON(jelement), StandardCharsets.UTF_8);

        URL ref = this.getClass().getResource("finduposKO.json");

        Assert.assertEquals(String.format("Find upos error return incorrect\n ref: %s\n res: %s\n", ref.toString(), out.toString()),
                FileUtils.readFileToString(new File(ref.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(out, StandardCharsets.UTF_8));
    }

    @Test
    public void test36Undo() throws IOException {
        name("modifying UPOS and Lemma, followed by undo");
        ce.setCallcitcommot(false);
        ce.setBacksuffix(".3");
        ce.process("mod lemma 1 Sammie", 13, "editinfo");
        ce.process("mod upos 2 VERBPAST", 13, "editinfo");
        ce.process("mod undo", 13, "editinfo");

        URL ref = this.getClass().getResource("test.mod.undo.conllu");
        URL res = this.getClass().getResource("test.conllu.3");
        Assert.assertEquals(String.format("mod undo output incorrect\n ref: %s\n res: %s\n", ref.toString(), res.toString()),
                FileUtils.readFileToString(new File(ref.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(new File(res.getFile()), StandardCharsets.UTF_8));
    }

    @Test
    public void test37AddED() throws IOException {
        name("adding/deleting enhanced dependency");
        ce.setCallcitcommot(false);
        ce.setBacksuffix(".4");
        ce.process("mod ed add 7 6 ref", 7, "editinfo");
        ce.process("mod ed add 8 6 nsubj", 7, "editinfo");
        ce.process("mod ed del 1 4", 11, "editinfo");

        URL ref = this.getClass().getResource("test.add_ed.conllu");
        URL res = this.getClass().getResource("test.conllu.4");

        Assert.assertEquals(String.format("mod ed incorrect\n ref: %s\n res: %s\n", ref.toString(), res.toString()),
                FileUtils.readFileToString(new File(ref.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(new File(res.getFile()), StandardCharsets.UTF_8));
    }

    @Test
    public void test41validUPOS() throws IOException {
        name("testing valid UPOS");
        ce.setCallcitcommot(false);
        URL url = this.getClass().getResource("upos.txt");
        File file = new File(url.getFile());
        List<String>filenames = new ArrayList<>();
        filenames.add(file.toString());
        ce.setValidUPOS(filenames);

        String rtc = ce.process("read 2", 1, "");
        JsonElement jelement = JsonParser.parseString(rtc); 

        File out = new File(folder, "uposvalid.json");
        //System.err.println(prettyprintJSON(jelement));
        FileUtils.writeStringToFile(out, prettyprintJSON(jelement), StandardCharsets.UTF_8);

        URL ref = this.getClass().getResource("uposvalid.json");

        Assert.assertEquals(String.format("Read return incorrect\n ref: %s\n res: %s\n", ref.toString(), out.toString()),
                FileUtils.readFileToString(new File(ref.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(out, StandardCharsets.UTF_8));
    }

    @Test
    public void test42validDeprel() throws IOException {
        name("testing valid deprels");
        ce.setCallcitcommot(false);
        URL url = this.getClass().getResource("deprel.txt");
        File file = new File(url.getFile());
        List<String>filenames = new ArrayList<>();
        filenames.add(file.toString());
        ce.setValidDeprels(filenames);

        String rtc = ce.process("read 2", 1, "");
        JsonElement jelement = JsonParser.parseString(rtc); 

        File out = new File(folder, "deprelvalid.json");
        //System.err.println(prettyprintJSON(jelement));
        FileUtils.writeStringToFile(out, prettyprintJSON(jelement), StandardCharsets.UTF_8);

        URL ref = this.getClass().getResource("deprelvalid.json");

        Assert.assertEquals(String.format("Read return incorrect\n ref: %s\n res: %s\n", ref.toString(), out.toString()),
                FileUtils.readFileToString(new File(ref.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(out, StandardCharsets.UTF_8));
    }

    @Test
    public void test43validFeuts() throws IOException {
        name("testing valid features");
        ce.setCallcitcommot(false);
        URL url = this.getClass().getResource("feat_val.txt");
        File file = new File(url.getFile());
        List<String>filenames = new ArrayList<>();
        filenames.add(file.toString());
        ce.setValidFeatures(filenames);

        String rtc = ce.process("read 1", 1, "");
        JsonElement jelement = JsonParser.parseString(rtc); 

        File out = new File(folder, "featsvalid.json");
        //System.err.println(prettyprintJSON(jelement));
        FileUtils.writeStringToFile(out, prettyprintJSON(jelement), StandardCharsets.UTF_8);

        URL ref = this.getClass().getResource("featsvalid.json");

        Assert.assertEquals(String.format("Read return incorrect\n ref: %s\n res: %s\n", ref.toString(), out.toString()),
                FileUtils.readFileToString(new File(ref.getFile()), StandardCharsets.UTF_8),
                FileUtils.readFileToString(out, StandardCharsets.UTF_8));
    }


//    @Test
//    public void test41AddExtraColumn() throws IOException, ConllException {
//        name("adding extra columns");
//        ce.setCallcitcommot(false);
//
//        URL url = this.getClass().getResource("test.conllu");
//        ConllFile cf = new ConllFile(new FileInputStream(url.getFile()));
//        ConllSentence csent = cf.getSentences().get(1);
//        ConllWord cw = csent.getWord(1);
//        cw.addExtracolumn(13, "B:ADDED13");
//        cw = csent.getWord(2);
//        cw.addExtracolumn(13, "I:ADDED13");
//        cw.addExtracolumn(12, "B:ADDED12");
//
//        File out = new File(folder,  "test_added.conllu");
//        FileUtils.writeStringToFile(out, csent.toString(), StandardCharsets.UTF_8);
//        ConllSentence csent2 = new ConllSentence(csent);
//        FileUtils.writeStringToFile(out, csent2.toString(), StandardCharsets.UTF_8, true);
//
//        URL urlref = this.getClass().getResource("added.conllu");
//
//        Assert.assertEquals(String.format("addin extracolumn incorrect\n ref: %s\n res: %s\n", url.toString(), out.toString()),
//                FileUtils.readFileToString(new File(urlref.getFile()), StandardCharsets.UTF_8),
//                FileUtils.readFileToString(out, StandardCharsets.UTF_8));
//    }

    private String prettyprintJSON(JsonElement j) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(j);
    }

}
