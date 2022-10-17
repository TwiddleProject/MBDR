import React from "react";
import Head from 'next/head'
import Image from 'next/image'
import styles from '../styles/Home.module.css'
import { Center, Text, Textarea, Box } from '@chakra-ui/react'
import { Stack, HStack, VStack, Flex, Square, Spinner } from '@chakra-ui/react'
import { Heading, Button } from '@chakra-ui/react'
import reactDom from 'react-dom'
import axios from "axios";
import { responseSymbol } from "next/dist/server/web/spec-compliant/fetch-event";

const baseURL = "https://mbdr.herokuapp.com";
// const baseURL = "http://localhost:8080";
export default function Home() {
  let [value, setValue] = React.useState('p => b\nb |~ f\np |~ !f')
  let [result, setResult] = React.useState(null)
  let [loading, setLoading] = React.useState(false);

  let handleInputChange = (e) => {
    let inputValue = e.target.value
    setValue(inputValue)
  }

  let handleGetRankedModel = () => {
    setLoading(true);
    axios.post(baseURL + "/rankedmodelrc", {
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

    );

  }

  return (
    <div className={styles.container}>
      <Head>
        <title>MBDR</title>
        <meta name="description" content="Project investigating model-based approaches to computing defeasible entailment." />
        <link rel="icon" href="/static/favicon.ico" />
      </Head>
      <Center h='100vh' >
        <Box w="xl" borderWidth='2px' borderRadius='lg' p="4" >
          <Flex direction="column" h="xl" gap='2'>
            <Flex flex='1' direction="column">
              <Heading as='h4' size='md' mb="2">
                Enter Knowledge Base:
              </Heading>
              <Textarea
                value={value}
                onChange={handleInputChange}
                // placeholder='p => b&#10;b |~ f&#10;p |~ !f&#10;'
                size='sm'
                mb="2"
                flex="1"
              />
              <Button colorScheme='teal' variant='solid' onClick={handleGetRankedModel}>
                Construct Rational Closure Ranked Model
              </Button>
            </Flex>
            <Flex flex='1' direction="column" >
              {loading && <Center h='100%'>
                <Spinner size='xl' />
              </Center>}
              {result && !loading && <><Heading as='h4' size='md' mb="2">
                Ranked Model:
              </Heading>
                <Textarea
                  value={result}
                  // placeholder='p => b&#10;b |~ f&#10;p |~ !f&#10;'
                  size='sm'
                  flex='1'
                /></>}
            </Flex>
          </Flex>

        </Box>

      </Center>
    </div >
  )
}
