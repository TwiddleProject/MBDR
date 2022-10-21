import React from "react";
import Head from 'next/head'
import Image from 'next/image'
import styles from '/styles/Home.module.css'
import { Center, Text, Textarea, Box, Select } from '@chakra-ui/react'
import { Stack, HStack, VStack, Flex, Square, Spinner } from '@chakra-ui/react'
import { Heading, Button } from '@chakra-ui/react'
import reactDom from 'react-dom'
import axios from "axios";
import { responseSymbol } from "next/dist/server/web/spec-compliant/fetch-event";

import Navbar from "/components/navbar/Navbar"
import { MdBuild } from "react-icons/md"

const baseURL = "https://app.twiddleproject.com/api";

const CONSTRUCTION_ALGORITHMS = [
  { "value" : "rationalclosure",
    "name" : "Rational Closure",
    "representations" : [
      {
        "value" : "modelrank",
        "name" : "Ranked Model"},
      {
        "value" : "formularank", 
        "name" : "Formula Model"},
      {
        "value" : "cumulativeformularank", 
        "name" : "Cumulative Formula Model"},
    ]
  },
  { "value" : "lexicographicclosure",
    "name" : "Lexicographic Closure",
    "representations" : [
      {
        "value" : "lexicographicmodelrank",
        "name" : "Ranked Model"},
      {
        "value" : "lexicographicformularank", 
        "name" : "Formula Model"},
      {
        "value" : "lexicographiccumulativeformularank", 
        "name" : "Cumulative Formula Model"},
    ]
  }
]

function getRepresentations(value){
  for(let closure of CONSTRUCTION_ALGORITHMS){
    if(closure.value === value){
      return closure.representations;
    }
  }
  return [];
}

function getFirstOption(closure){
  let options = getRepresentations(closure);
  return options.length === 0 ? "" : options[0].value;
}

export default function Home() {
  let [knowledgeBase, setKnowledgeBase] = React.useState('p => b\nb |~ f\np |~ !f');
  let [{closure, model}, setAlgorithm] = React.useState(
    {
      closure : CONSTRUCTION_ALGORITHMS[0].value,
      model : CONSTRUCTION_ALGORITHMS[0].representations[0].value
    }
  );
  let [result, setResult] = React.useState(null);
  let [loading, setLoading] = React.useState(false);

  let handleInputChange = (e) => {
    let inputValue = e.target.value
    setKnowledgeBase(inputValue)
  }

  let handleGetRankedModel = () => {
    setLoading(true);
    axios.post(baseURL + "/construction/" + model, {
      data: knowledgeBase, headers: {
        "Access-Control-Allow-Origin": "*", 'Accept': 'application/json',
        'Content-Type': 'application/json'
      }
    }).then(
      (response) => {
        setLoading(false)
        console.log(response);
        setResult(response.data);
      }
    ).catch(
      (error) => {
        setLoading(false);
        console.log(error);
        setResult("Could not build model!");
      }
    );

  }

  let handleClosureChange = (event) => {
    setAlgorithm(last => (
      {model: getFirstOption(event.target.value), closure: event.target.value}
    ));
  }

  let handleModelChange = (event) => {
    setAlgorithm(last => (
      {...last, model: event.target.value}
    ));
  }

  return (
      <>
        <Head>
          <title>Twiddle App | Construction</title>
          <meta name="description" content="Project investigating model-based approaches to computing defeasible entailment." />
          <link rel="icon" href="/static/img/favicon.ico" />
        </Head>
        <Navbar selected="Construction" w="100vw"></Navbar>
        <div className={styles.container}>
        <Center mt='16'>
          <Flex flex='1' direction="column" alignItems="center">
            <Box>
              <Heading size='lg' mb="10" noOfLines={2}>
                Model Constructor
              </Heading>
            </Box>
            <Box w={["xs", "sm", "lg", "xl"]} borderWidth='2px' borderRadius='lg' p="4" >
              <Flex direction="column" h="650px" gap='2'>
                <Flex flex='1' direction="column" align="center">
                  <Heading as='h4' size='sm' mb="2">
                    Knowledge Base
                  </Heading>
                  <Textarea
                    value={knowledgeBase}
                    onChange={handleInputChange}
                    fontFamily="monospace"
                    size='sm'
                    mb="2"
                    flex="1"
                  />
                  <Flex align={['center']} justify={["center"]} direction={['column']}>
                    <Flex direction={['column', null, 'row']}>
                      <Select w={[null, null, null, '250px']} value={closure} onChange={handleClosureChange}>
                        {CONSTRUCTION_ALGORITHMS.map(opt => 
                          <option key={opt.value} value={opt.value}>{opt.name}</option>
                        )}
                      </Select>
                      <Select w={[null, null, null, '250px']} value={model} onChange={handleModelChange}>
                        {getRepresentations(closure).map(opt => 
                          <option key={opt.value} value={opt.value}>{opt.name}</option>
                        )}
                      </Select>
                    </Flex>
                    <Button my="2" leftIcon={<MdBuild />} 
                      colorScheme="twitter"
                      color='white'
                      borderRadius="140px"
                      w={["150px", "200px"]}
                      variant='solid' onClick={handleGetRankedModel}>
                      Construct
                    </Button>
                  </Flex>
                </Flex>
                <Flex flex='1' direction="column" align="center">
                  {loading && <Center h='100%'>
                    <Spinner size='xl' />
                  </Center>}
                  {result && !loading && <><Heading as='h4' size='md' mb="2">
                    Model
                  </Heading>
                    <Textarea
                      value={result}
                      fontFamily="monospace"
                      size='sm'
                      flex='1'
                    /></>}
                </Flex>
              </Flex>
            </Box>
          </Flex>
        </Center>
      </div >
    </>
  )
}
