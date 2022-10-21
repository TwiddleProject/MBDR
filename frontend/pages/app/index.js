import React from "react";
import Head from 'next/head'
import Image from 'next/image'
import styles from '/styles/Home.module.css'
import { Center, Text, Textarea, Box, Select, Link } from '@chakra-ui/react'
import { Stack, HStack, VStack, Flex, Square, Spinner } from '@chakra-ui/react'
import { Heading, Button } from '@chakra-ui/react'
import reactDom from 'react-dom'
import axios from "axios";
import { responseSymbol } from "next/dist/server/web/spec-compliant/fetch-event";

import Navbar from "/components/navbar/Navbar"
import { MdBuild, MdOutlineLiveHelp, MdOutlineCode} from "react-icons/md"

const baseURL = "https://app.twiddleproject.com/api";

export default function Home() {

  return (
      <>
        <Head>
          <title>Twiddle App</title>
          <meta name="description" content="Project investigating model-based approaches to computing defeasible entailment." />
          <link rel="icon" href="/static/img/favicon.ico" />
        </Head>
        <Navbar selected="Home" w="100vw"></Navbar>
        <div className={styles.container}>
        <Center mt='16' px="6">
          <Flex w={["xs", "sm", "lg", "3xl"]} direction="column" justify="start" align="center">
          <Heading size='lg' mb={["5", null, "7", "10"]} noOfLines={2}>
          Welcome to Twiddle App
          </Heading>
          <Text my="5" fontSize={['md', 'lg']}>
            As part of the <Link color="blue" href="https://twiddleproject.com">Model-based Defeasible Reasoning</Link> project, we developed new algorithms for constructing ranked models and using them for query checking.  
            Twiddle App provides a simple user interface for running these algorithms!
          </Text>
          <Text my="5" fontSize={['md', 'lg']}>
            Given a knowledge base (information about the world), we can construct models that encode common reasoning patterns. It is then possible to check whether statements (called queries) are valid conclusions via a process called entailment.
          </Text>
          <Flex my={["3", null, "5", "8"]}>
            <Link style={{"text-decoration": "none"}} href="/app/construction" isExternal={false}>
            <Button 
              borderRadius="140px" 
              w={["150px", "175px"]}
              leftIcon={<MdBuild />} 
              colorScheme='twitter' 
              mr="3">
              Contruct
            </Button>
            </Link>
            <Link style={{"text-decoration": "none"}} href="/app/entailment" isExternal={false}>
            <Button
              borderRadius="140px" 
              w={["150px", "175px"]}
              leftIcon={<MdOutlineLiveHelp />} 
              colorScheme='twitter' 
              variant='outline'
              mr="3">
              Query
            </Button>
            </Link>
          </Flex>  
          <Text my="5" fontSize={['md', 'lg']}>
            The algorithms are all implemented in Java using the <Link color="blue" href="https://tweetyproject.org">TweetyProject</Link> logic libraries. The project source code is available on GitHub.
          </Text>
          <Flex my={["3", null, "5", "8"]} jusify="center">
            <Link style={{"text-decoration": "none"}} href="https://github.com/TwiddleProject" isExternal={true}>
            <Button
              borderRadius="140px" 
              w={["150px", "175px"]}
              leftIcon={<MdOutlineCode />} 
              colorScheme='blue' 
              mr="3">
              Source Code
            </Button>
            </Link>
          </Flex>
          </Flex>
        </Center>
      </div >
    </>
  )
}
