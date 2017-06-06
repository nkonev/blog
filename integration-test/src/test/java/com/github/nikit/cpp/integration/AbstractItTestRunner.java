package com.github.nikit.cpp.integration;

/**
 * Created by nik on 27.05.17.
 */

import com.github.nikit.cpp.Launcher;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(
        classes = {Launcher.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
public abstract class AbstractItTestRunner {

}
