import React from 'react';
import styled from '@emotion/styled';
import * as Button from '../atoms/Button';

const Toolbar = styled.div({
  gridArea: 'Toolbar',
  display: 'grid',
  alignItems: 'center',
  gridTemplateColumns: '1fr 1fr 1fr',
  color: '#fff',
  backgroundColor: '#333',
  justifyContent: 'space-between',
});

const RoomLable = styled.div({
  maxWidth: 100,
  whiteSpace: 'nowrap',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
});

const Left = styled.div({
  display: 'flex',
  // display: 'grid',
  alignItems: 'center',
});

const Center = styled.div({
  textAlign: 'center',
});

const Right = styled.div({
  textAlign: 'right',
});

const LogoImg = styled.img({
  height: 40,
});

export const View: React.FC = ({ children }) => (
  <Toolbar>
    <Left>
      <a href="https://openvidu.io/" target="_blank">
        <LogoImg alt="OpenVidu Logo" src="images/openvidu_logo.png" />
      </a>
      <RoomLable>{children}</RoomLable>
    </Left>
    <Center>
      <Button.IconButton>
        <i className="material-icons">mic</i>
      </Button.IconButton>
      <Button.IconButton>
        <i className="material-icons">videocam</i>
      </Button.IconButton>
      <Button.IconButton>
        <i className="material-icons">screen_share</i>
      </Button.IconButton>
      <Button.IconButton>
        <i className="material-icons">fullscreen</i>
      </Button.IconButton>
      <Button.IconButton>
        <i className="material-icons">power_settings_new</i>
      </Button.IconButton>
    </Center>
    <Right>
      <Button.IconButton>
        <i className="material-icons">chat</i>
      </Button.IconButton>
    </Right>
  </Toolbar>
);
