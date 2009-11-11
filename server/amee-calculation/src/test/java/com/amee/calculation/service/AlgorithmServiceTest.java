/*
 * This file is part of AMEE.
 *
 * Copyright (c) 2007, 2008, 2009 AMEE UK LIMITED (help@amee.com).
 *
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
package com.amee.calculation.service;

import com.amee.domain.algorithm.Algorithm;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class AlgorithmServiceTest {

    @Autowired
    private AlgorithmService algorithmService;

    @Test
    public void reallySimpleAlgorithmOK() throws ScriptException {
        Algorithm algorithm = new Algorithm();
        algorithm.setContent("1");
        Map<String, Object> values = new HashMap<String, Object>();
        assertTrue("Really simple algorithm should be OK.", algorithmService.evaluate(algorithm, values) != null);
    }

    @Test
    public void emptyAlgorithmNotOK() throws ScriptException {
        Algorithm algorithm = new Algorithm();
        algorithm.setContent("");
        Map<String, Object> values = new HashMap<String, Object>();
        try {
            algorithmService.evaluate(algorithm, values);
            fail("Empty algorithm should NOT be OK.");
        } catch (Throwable t) {
            // swallow
        }
    }
}