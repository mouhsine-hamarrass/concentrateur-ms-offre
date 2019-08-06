package com.sofrecom.sofid.boundedcontext.web.rest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.vanroy.springboot.autoconfigure.data.jest.ElasticsearchJestAutoConfiguration;
import com.sofrecom.sofid.BoundedContextApp;
import com.sofrecom.sofid.boundedcontext.InitTestDB;
import com.sofrecom.sofid.boundedcontext.domain.BoundedContext;
import com.sofrecom.sofid.boundedcontext.messaging.service.MessagingService;
import com.sofrecom.sofid.boundedcontext.process.BoundedContextProcess;
import com.sofrecom.sofid.boundedcontext.remote.service.ReferenceDataClient;
import com.sofrecom.sofid.boundedcontext.repository.BoundedContextRepository;
import com.sofrecom.sofid.boundedcontext.service.BoundedContextService;
import com.sofrecom.sofid.framework.context.UserPrincipal;
import com.sofrecom.sofid.framework.dto.SelectItemDto;
import com.sofrecom.sofid.framework.exception.RestErrorHandler;
import com.sofrecom.sofid.framework.security.configuration.api.AuthenticationToken;

/**
 * Test class for {@link BoundedContextResource}.
 *
 * @author sofid@sofrecom.com
 * @author Mehdi JEBBARI  <mehdi.jebbari@sofrecom.com>
 */
@RunWith(SpringRunner.class)
@EnableAutoConfiguration(exclude = ElasticsearchJestAutoConfiguration.class)
@SpringBootTest(classes = BoundedContextApp.class)
@AutoConfigureMockMvc
@InitTestDB
public class BoundedContextResourceIT {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    BoundedContextProcess userProcess;

    @Autowired
    BoundedContextService userService;

    @Autowired
    BoundedContextRepository userRepository;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private RestErrorHandler restErrorHandler;

    @Autowired
    private Validator validator;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @MockBean
    ReferenceDataClient referenceDataClient;

    @MockBean
    MessagingService messagingService;

    @Autowired
    ObjectMapper objectMapper;

    @Before
    public void init() {
        Mockito.when(referenceDataClient.select(3, null, Arrays.asList(2), null)).thenReturn(Arrays.asList(SelectItemDto.from(2, "Passport")));
        Mockito.when(referenceDataClient.select(3, null, Arrays.asList(1), null)).thenReturn(Arrays.asList(SelectItemDto.from(1, "National ID")));
        Mockito.when(referenceDataClient.select(5, null, Arrays.asList(3, 5), null)).thenReturn(Arrays.asList(SelectItemDto.from(3, "Trusted"), SelectItemDto.from(5, "AHA")));
        Mockito.when(referenceDataClient.select(5, null, Arrays.asList(3, 4), null)).thenReturn(Arrays.asList(SelectItemDto.from(3, "Trusted"), SelectItemDto.from(4, "Point of sales")));
        Mockito.when(referenceDataClient.select(5, null, Arrays.asList(4), null)).thenReturn(Arrays.asList(SelectItemDto.from(4, "Point of sales")));
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final BoundedContextResource userResource = new BoundedContextResource(userProcess);
        this.mockMvc = MockMvcBuilders.standaloneSetup(userResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(restErrorHandler)
                .setConversionService(createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter)
                .setValidator(validator).build();

        BoundedContext boundedcontext = userService.get(1);
        UserPrincipal userPrincipal = new UserPrincipal(boundedcontext.getId(),boundedcontext.getUsername()
        ,new HashSet<>());
        AuthenticationToken authenticationToken =
                new AuthenticationToken("jwt",null);
        authenticationToken.setAuthenticated(true);
        authenticationToken.setUserPrincipal(userPrincipal);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

 
    /**
     * Create a {@link FormattingConversionService} which use ISO date format, instead of the localized one.
     *
     * @return the {@link FormattingConversionService}.
     */
    public static FormattingConversionService createFormattingConversionService() {
        DefaultFormattingConversionService dfcs = new DefaultFormattingConversionService();
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setUseIsoFormat(true);
        registrar.registerFormatters(dfcs);
        return dfcs;
    }
    
    
    @Test
    public void testGet() throws Exception{

        Map expectedResult=  new HashMap() {{
            put("id", 1);
            put("username","usertest");
            put("password","passTest");
        }};

        mockMvc.perform(get("/api/v1/boundedcontext/1")
                .accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id",is(expectedResult.get("id"))))
                .andExpect(jsonPath("$.username",is(expectedResult.get("username"))))
                .andExpect(jsonPath("$.password",is(expectedResult.get("password"))));
    	//assertTrue(true);
        
    }
    
   
}


