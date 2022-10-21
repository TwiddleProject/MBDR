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
import { MdBuild, MdOutlineLiveHelp} from "react-icons/md"

const baseURL = "https://app.twiddleproject.com/api";

const ENTAILMENT_ALGORITHMS = [
  { "value" : "cumulativeformularank",
    "name" : "Rational Closure"
  },
  { "value" : "lexicographicmodelrank",
    "name" : "Lexicographic Closure"
  }
]

export default function Home() {
  let [knowledgeBase, setKnowledgeBase] = React.useState('p => b\nb |~ f\np |~ !f');
  let [querySet, setQuerySet] = React.useState('b |~ p\nf |~ b\n!f |~ !p');
  let [algorithm, setAlgorithm] = React.useState(ENTAILMENT_ALGORITHMS[0].value);
  let [result, setResult] = React.useState(null);
  let [loading, setLoading] = React.useState(false);

  let handleKnowledgeBaseChange = (e) => {
    let inputValue = e.target.value
    setKnowledgeBase(inputValue)
  }

  let handleQuerySetChange = (e) => {
    let inputValue = e.target.value
    setQuerySet(inputValue)
  }

  let handleAlgorithmChange = (event) => {
    setAlgorithm(event.target.value);
  }

  let handleGetAnswers = () => {
    setLoading(true);
    axios.post(baseURL + "/entailment/" + algorithm, {
      knowledge: knowledgeBase, queries: querySet,
      headers: {
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
        setResult("Could not answer queries!");
      }
    );

  }

  return (
      <>
        <Head>
          <title>Twiddle App | Entailment</title>
          <meta name="description" content="Project investigating model-based approaches to computing defeasible entailment." />
          <link rel="icon" href="/static/img/favicon.ico" />
        </Head>
        <Navbar selected="Entailment" w="100vw"></Navbar>
        <div className={styles.container}>
        <Center mt='16'>
          <Flex flex='1' direction="column" alignItems="center">
            <Box>
              <Heading size='lg' mb="10" noOfLines={2}>
                Entailment Checker
              </Heading>
            </Box>
            <Box w={["xs", "sm", "lg", "xl"]} borderWidth='2px' borderRadius='lg' p="4">
              <Flex direction="column" h="650px" gap='2'>
                <Flex flex='1' direction="column" align="center" justify="stretch">
                  <Heading as='h4' size='sm' mb="2">
                    Knowledge Base
                  </Heading>
                  <Textarea
                    value={knowledgeBase}
                    onChange={handleKnowledgeBaseChange}
                    fontFamily="monospace"
                    size='sm' 
                    mb="2"
                    flex="1"
                  />
                  <Heading as='h4' size='sm' mb="2">
                    Query Set
                  </Heading>
                  <Textarea
                    value={querySet}
                    onChange={handleQuerySetChange}
                    fontFamily="monospace"
                    size='sm'
                    mb="2"
                    flex="1"
                  />
                  <Flex align={['center']} justify={["center"]} direction={['column', null, 'row']}>
                    <Select mr={[null, null, '4']} w={[null, null, null, '250px']} value={algorithm} onChange={handleAlgorithmChange}>
                      {ENTAILMENT_ALGORITHMS.map(opt => 
                        <option key={opt.value} value={opt.value}>{opt.name}</option>
                      )}
                    </Select>
                    <Button my="2" leftIcon={<MdOutlineLiveHelp />} 
                      colorScheme="twitter"
                      color='white'
                      borderRadius="140px"
                      w={["150px", "200px"]}
                      variant='solid' onClick={handleGetAnswers}>
                      Check
                    </Button>
                  </Flex>
                </Flex>
                <Flex flex='1' direction="column" align="center">
                  {loading && <Center h='100%'>
                    <Spinner size='xl' />
                  </Center>}
                  {result && !loading && <><Heading as='h4' size='md' mb="2">
                    Answers
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
