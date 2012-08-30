/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.definitions;


import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Query;
import org.drools.definition.type.FactField;
import org.drools.definition.type.FactType;
import org.drools.definitions.rule.impl.GlobalImpl;
import org.drools.io.impl.ByteArrayResource;
import org.junit.Test;

import static org.junit.Assert.*;

public class KnowledgePackageMetaDataTest {


    private String drl ="" +
            "package org.drools.test.definitions \n" +
            "import java.util.List; \n" +
            "\n" +
            "global Integer N; \n" +
            "global List list; \n" +
            "\n" +
            "function void fun1() {}\n" +
            "\n" +
            "function String fun2( int j ) { return null; } \n" +
            "\n" +
            "declare Person\n" +
            "  name : String\n" +
            "  age  : int\n" +
            "end\n" +
            "\n" +
            "declare Foo extends Person\n" +
            "   bar : String\n" +
            "end \n" +
            "\n" +
            "query qry1() \n" +
            "  Foo()\n" +
            "end\n" +
            "\n" +
            "query qry2( String x )\n" +
            "  x := String()\n" +
            "end\n" +
            "\n" +
            "rule \"rule1\"\n" +
            "when\n" +
            "then\n" +
            "end\n" +
            "\n" +
            "rule \"rule2\"\n" +
            "when\n" +
            "then\n" +
            "end";

    @Test
    public void testMetaData() {
        KnowledgeBuilder kBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kBuilder.add( new ByteArrayResource( drl.getBytes() ), ResourceType.DRL );
        if ( kBuilder.hasErrors() ) {
            fail( kBuilder.getErrors().toString() );
        }

        KnowledgeBase kBase = KnowledgeBaseFactory.newKnowledgeBase();
        kBase.addKnowledgePackages( kBuilder.getKnowledgePackages() );
        KnowledgePackage pack = kBase.getKnowledgePackage( "org.drools.test.definitions" );

        assertNotNull( pack );

        assertEquals( 2, pack.getFunctionNames().size() );
        assertTrue( pack.getFunctionNames().contains( "fun1" ) );
        assertTrue( pack.getFunctionNames().contains( "fun2" ) );

        assertEquals( 2, pack.getGlobalVariables().size() );
        GlobalImpl g1 = new GlobalImpl( "N", "java.lang.Integer" );
        GlobalImpl g2 = new GlobalImpl( "list", "java.util.List" );
        assertTrue( pack.getGlobalVariables().contains( g1 ) );
        assertTrue( pack.getGlobalVariables().contains( g2 ) );

        assertEquals( 2, pack.getFactTypes().size() );
        FactType type;
        for ( int j = 0; j < 2; j++ ) {
            type = pack.getFactTypes().iterator().next();
            if ( type.getName().equals( "org.drools.test.definitions.Person" ) ) {
                assertEquals( 2, type.getFields().size() );
            } else if (type.getName().equals( "org.drools.test.definitions.Foo" ) ) {
                assertEquals( "org.drools.test.definitions.Person", type.getSuperClass() );

                FactField fld = type.getField( "bar" );
                assertEquals( 3, fld.getIndex() );
                assertEquals( String.class, fld.getType() );
            } else {
                fail("Unexpected fact type " + type);
            }
        }


        assertEquals( 2, pack.getQueries().size() );
        for ( Query q : pack.getQueries() ) {
            assertTrue( q.getName().equals( "qry1" ) || q.getName().equals( "qry2" ) );
        }

        assertEquals( 4, pack.getRules().size() );
        assertTrue( pack.getRules().containsAll( pack.getQueries() ) );

    }


}