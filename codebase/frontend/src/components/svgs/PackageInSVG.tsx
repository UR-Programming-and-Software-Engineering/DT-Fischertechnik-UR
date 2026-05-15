interface SVGProps {
  fillColor?: string;
  [key: string]: any;
}

const PackageInSVG = ({ fillColor = '#000000', ...props }: SVGProps) => (
  <svg
    width="100%"
    height="100%"
    viewBox="0 0 436 132"
    xmlns="http://www.w3.org/2000/svg"
    xmlnsXlink="http://www.w3.org/1999/xlink"
    xmlSpace="preserve"
    style={{
      fillRule: 'evenodd',
      clipRule: 'evenodd',
      strokeLinecap: 'round',
      strokeLinejoin: 'round',
      strokeMiterlimit: 2,
    }}
    {...props}
  >
    <path
      d="M8.333,65.571l275.406,-0"
      style={{
        fill: 'none',
        stroke: '#7b7b7b',
        strokeWidth: '16.67px',
      }}
    />
    <path
      d="M426.9,65.571l-114.474,57.237l-0,-114.475l114.474,57.238Z"
      style={{
        fill: '#808080',
        fillOpacity: 0.02,
        stroke: '#7b7b7b',
        strokeWidth: '16.67px',
      }}
    />
    <g transform="matrix(50,0,0,50,279.14,39.8804)" />
    <text
      x="20.669px"
      y="39.88px"
      style={{
        fontFamily: "'ArialMT', 'Arial', sans-serif",
        fontSize: 50,
        fill: '#7b7b7b',
        stroke: '#7b7b7b',
        strokeWidth: '3.13px',
        strokeLinecap: 'butt',
      }}
    >
      {'Package IN'}
    </text>
  </svg>
);
export default PackageInSVG;
