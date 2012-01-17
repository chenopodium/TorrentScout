/*
*	Copyright (C) 2011 Life Technologies Inc.
*
*   This program is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 2 of the License, or
*   (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.scoreview;

import com.iontorrent.heatmaps.ScoreMaskGenerator;
import com.iontorrent.wellmodel.WellContext;
import com.iontorrent.results.scores.ScoreMask;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.GlobalContext;
import com.iontorrent.rawdataaccess.wells.ScoreMaskFlag;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Chantal Roth
 */
public class ScoreMaskGeneratorTest {

    static String res_dir = "S:/data/beverly/results/";
    static String raw_dir = "S:/data/beverly/raw/";
    static String cache_dir = "S:/data/beverly/cache/";
    
    ScoreMaskFlag flag = ScoreMaskFlag.IDENTITY;
    ScoreMaskGenerator instance;

    public ScoreMaskGeneratorTest() {
        GlobalContext global = new GlobalContext();
        global.setContext("localhost");
        
        ExperimentContext exp = ExperimentContext.createFake(global);
        exp.setResDirFromDb(res_dir);
       
        WellContext wellContext = exp.createWellContext();
        ScoreMask mask = ScoreMask.getMask(exp, wellContext);
        instance = new ScoreMaskGenerator(mask, exp);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

 

    @Test
    public void testReadSamParsedFile() throws Exception {
        System.out.println("readSamParsedFile");
        String result = instance.processBamFile(false);
     
    }
}
