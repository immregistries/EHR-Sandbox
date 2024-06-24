import { BaseFormOption } from "./structure";

/**
 * Interface for codemaps lists
 */


export interface CodeMap {
  codeBaseMap: CodeBaseMap;
}
export interface CodeBaseMap {
  [key: string]: CodeSet;
}
export interface CodeSet {
  [key: string]: Code;
}
export interface Code {
  "value": string;
  "label": string;
  "description"?: string;
  "codeStatus"?: CodeStatus;
  "reference"?: CodeReference;
  "useDate"?: Date;
  "useAge"?: string;
  "conceptType"?: string;
  "testAge"?: string;
}

export interface CodeStatus {
  status?: string
}
export interface CodeReference {
  linkTo: CodeReferenceLink[];
}

export interface CodeReferenceLink {
  value: string;
  codeset: string;
}

export interface CodeReferenceTable {
  [origin_label: string]: CodeReferenceTableMember
}
export interface CodeReferenceTableMember {
  reference: CodeReference;
  value: string;
}
