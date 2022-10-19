import React from "react";
import Head from 'next/head'
import Image from 'next/image'
import styles from '../styles/Home.module.css'
import { Center, Text, Textarea, Box, Select } from '@chakra-ui/react'
import { Stack, HStack, VStack, Flex, Square, Spinner } from '@chakra-ui/react'
import { Heading, Button } from '@chakra-ui/react'
import reactDom from 'react-dom'
import axios from "axios";
import { responseSymbol } from "next/dist/server/web/spec-compliant/fetch-event";

import Navbar from "../components/navbar/Navbar"
import { MdBuild } from "react-icons/md"

const baseURL = "https://app.twiddleproject.com/api";

const CONSTRUCTION_ALGORITHMS = [
  {
    "value" : "modelrank", 
    "name" : "Rational Closure Model"},
  // {
  //   "value" : "formularank", 
  //   "name" : "Rational Closure Formula Rank"},
  // {
  //   "value" : "cumulativeformularank", 
  //   "name" : "Rational Closure Cumulative Formula Rank"},
  // {
  //   "value" : "lexicographicmodelrank",
  //   "name" : "Lexicographic Closure Model"},
  // {
  //   "value" : "lexicographicformularank", 
  //   "name" : "Lexicographic Closure Formula Model"},
  // {
  //   "value" : "lexicographiccumulativeformularank", 
  //   "name" : "Lexicographic Closure Cumulative Formula Model"},
]

export default function Home() {
  let [value, setValue] = React.useState('p => b\nb |~ f\np |~ !f')
  let [result, setResult] = React.useState(null);
  let [loading, setLoading] = React.useState(false);

  let handleInputChange = (e) => {
    let inputValue = e.target.value
    setValue(inputValue)
  }

  let handleGetRankedModel = () => {
    setLoading(true);
    axios.post(baseURL + "/construction/modelrank", {
      data: value, headers: {
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
        setResult(error);
      }
    );

  }

  return (
      <>
        <Head>
          <title>Twiddle App</title>
          <meta name="description" content="Project investigating model-based approaches to computing defeasible entailment." />
          <link rel="icon" href="/static/img/favicon.ico" />
        </Head>
        <Navbar w="100vw"></Navbar>
        <div className={styles.container}>
        <Center mt='16'>
          <Flex flex='1' direction="column" alignItems="center">
            <Box>
              <Heading size='lg' mb="10" noOfLines={2}>
                Knowledge Base Model Generator
              </Heading>
            </Box>
            <Box w={["xs", "sm", "lg"]} borderWidth='2px' borderRadius='lg' p="4" >
              <Flex direction="column" h="xl" gap='2'>
                <Flex flex='1' direction="column" align="center">
                  <Heading as='h4' size='md' mb="2">
                    Knowledge Base
                  </Heading>
                  <Textarea
                    value={value}
                    onChange={handleInputChange}
                    // placeholder='p => b&#10;b |~ f&#10;p |~ !f&#10;'
                    fontFamily="monospace"
                    size='sm'
                    mb="2"
                    flex="1"
                  />
                  <Flex align='center' justify="center" direction={['column', null, 'row']}>
                    <Select pr="2">
                      {CONSTRUCTION_ALGORITHMS.map(opt => 
                        <option key={opt.value} value={opt.value}>{opt.name}</option>
                      )}
                    </Select>
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
                    Rational Closure Model
                  </Heading>
                    <Textarea
                      value={result}
                      fontFamily="monospace"
                      // placeholder='p => b&#10;b |~ f&#10;p |~ !f&#10;'
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
