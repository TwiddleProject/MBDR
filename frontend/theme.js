import { extendTheme } from '@chakra-ui/react'

const config = {
    initialColorMode: 'light',
    useSystemColorMode: false,
}

const fonts = {
    heading: `'Open Sans', sans-serif`,
    body: `'Lato', sans-serif`
}

const theme = extendTheme({
  config,
  fonts
})

export default theme