import * as React from 'react';
interface SVGProps {
  fillColor?: string;
  [key: string]: any;
}

const storageSvg = ({ fillColor, ...props }: SVGProps) => (
  <svg
    width="100%"
    height="100%"
    viewBox="0 0 174 51"
    xmlns="http://www.w3.org/2000/svg"
    xmlnsXlink="http://www.w3.org/1999/xlink"
    xmlSpace="preserve"
    style={{
      fillRule: 'evenodd',
      clipRule: 'evenodd',
      strokeLinejoin: 'round',
      strokeMiterlimit: 2,
    }}
    {...props}
  >
    <g transform="matrix(50,0,0,50,174.438,37.9639)" />
    <text
      x="-0.684px"
      y="37.964px"
      style={{
        fontFamily: "'ArialMT', 'Arial', sans-serif",
        fontSize: 50,
        fill: '#7b7b7b',
        stroke: '#7b7b7b',
        strokeWidth: '3.13px',
      }}
    >
      {'Storage'}
    </text>
  </svg>
);
export default storageSvg;
